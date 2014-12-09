#include "stdafx.h"
#include "Utils.h"
#include <inttypes.h>
///TODO: Move all these to utils file



// returns -1 if fails. Works on both FILE* and char*. offset is used only in char*.
int WriteFileOrCharArr(const char* st, int bytes, void* writeTo, bool isFile, int offset)
{
	int ret = 1;
	if (isFile)
		ret = fwrite(st, sizeof(char), bytes, (FILE*)writeTo);
	else
	{
		if (bytes == 1)
			*((char*)writeTo + offset) = *st;
		else
		{
			//ret = _snprintf(((char*)writeTo + offset), bytes, "%s", st);
			memcpy(((char*)writeTo + offset), st, bytes);
		}
	}
	return ret;
}


//------------------------------------------------------------------------
// Wav related functions - Used by AudioRecorder and AudioServer
// to write wav header files. Copied from trunk version.
//------------------------------------------------------------------------
// G: write a number to a file, given an endianness
static void PutNum(long num, void *writeTo, int endianness, int bytes, bool isFile, int offset)
{
	int i;
	/*unsigned*/ char c;
	if (!endianness)
		i = 0;
	else
		i = bytes - 1;
	while (bytes--)
	{
		c = (num >> (i << 3)) & 0xff;
		if (WriteFileOrCharArr(&c, 1, writeTo, isFile, offset) == -1)
		{
			perror("Could not write to output.");
			exit(1);
		}
		offset++;
		if (endianness)
			i--;
		else
			i++;
	}
}


// G: writes a wav header to f (inspired from http://www.techband.in/2011/04/write-wav-header-in-cc.html)
// For explanations on the fields, visit https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
// SampleRate defaults to 8000
void WriteWavHeader(void *writeTo, int SampleRate, bool isFile)
{
	int bitsPerSample = 16;
	int writtenAccumulated = 0;
	if (writeTo != NULL){
		/* quick and dirty */
		WriteFileOrCharArr("RIFF", 4, writeTo, isFile, writtenAccumulated);               /*  0-3 */
		writtenAccumulated += 4;
		PutNum(3203383023, writeTo, 0, 4, isFile, writtenAccumulated);        /*  4-7 */ // 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size) === bytes + 36
		writtenAccumulated += 4;
		WriteFileOrCharArr("WAVEfmt ", 8, writeTo, isFile, writtenAccumulated);           /*  8-15 */
		writtenAccumulated += 8;
		PutNum(16, writeTo, 0, 4, isFile, writtenAccumulated);                /* 16-19 */
		writtenAccumulated += 4;
		PutNum(1, writeTo, 0, 2, isFile, writtenAccumulated);                 /* 20-21 */ //PCM=1
		writtenAccumulated += 2;
		PutNum(1, writeTo, 0, 2, isFile, writtenAccumulated);                 /* 22-23 */ //NumChannels=Mono=1
		writtenAccumulated += 2;
		PutNum(SampleRate, writeTo, 0, 4, isFile, writtenAccumulated);             /* 24-27 */ // our sampling rate is 8000
		writtenAccumulated += 4;
		PutNum(SampleRate * 1 * bitsPerSample / 8 /*Amos: fixed*/, writeTo, 0, 4, isFile, writtenAccumulated);         /* 28-31 */ //ByteRate == SampleRate * NumChannels * BitsPerSample/8
		writtenAccumulated += 4;
		PutNum(1 * bitsPerSample / 8, writeTo, 0, 2, isFile, writtenAccumulated);                 /* 32-33 */ // block align == NumChannels * BitsPerSample/8
		writtenAccumulated += 2;
		PutNum(bitsPerSample, writeTo, 0, 2, isFile, writtenAccumulated);                /* 34-35 */
		writtenAccumulated += 2;
		WriteFileOrCharArr("data", 4, writeTo, isFile, writtenAccumulated); /* 36-39 */
		writtenAccumulated += 4;
		// We write 'beefbeef'. This has to be replaced by the real value once we know
		// the size of the file.  see CloseWav()
		PutNum(3203383023, writeTo, 1, 4, isFile, writtenAccumulated);
		writtenAccumulated += 4;
	}
}

// G: closes a wave file (reads the size of file, and write it)
// totLengthBytes is required only for char*
void CloseWav(void *writeTo, bool isFile, int totLengthBytes)
{
	if (writeTo != NULL)
	{
		// We write the number of bytes in the wave header
		if (isFile)
		{
			FILE* f = (FILE*)writeTo;
			fseek(f, 0, SEEK_END);
			long totLengthBytes = ftell(f);
		}
		if (isFile)
			fseek((FILE*)writeTo, 4, SEEK_SET);
		PutNum(totLengthBytes - 8, writeTo, 0, 4, isFile, 4);  // We write ChunkSize
		if (isFile)
			fseek((FILE*)writeTo, 40, SEEK_SET);
		PutNum(totLengthBytes - WAV_HEADER_SIZE, writeTo, 0, 4, isFile, 40);  // We write SubchunkSize2
		if (isFile)
			fclose((FILE*)writeTo);
	}
}


void WriteToFile(char* data, char* filePath, int dataBytes)
{
	FILE* f;
	errno_t err = fopen_s(&f, filePath, "wb");
	if (err == 0)
	{
		fwrite(data, sizeof(char), dataBytes, f);
		fclose(f);
	}
	else
		printf("error opening file");

}

void ReduceAmplitude(char* buffer, int length, double reduceTo)
{
	int16_t* sample = (int16_t*)buffer;
	for (int i = 0; i < length / (sizeof(int16_t) / sizeof(char)); i++)
		sample[i] = sample[i] * reduceTo;
}