#include "stdafx.h"

//#define BUFFER_SIZE 4096
#define BUFFER_SIZE 4096

void usage(void);
std::wstring RunStreamingServer(int portNum);
std::wstring DecodeFromMem(IStream * pMemStream);

//all below should be moved to utils
static void PutNum(long num, void *writeTo, int endianness, int bytes, bool isFile, int offset);
void WriteWavHeader(void *writeTo, int SampleRate, bool isFile);
void CloseWav(void *writeTo, bool isFile, int totLengthBytes);
void WriteToFile(char* data, char* filePath, int dataBytes);
void ReduceAmplitude(char* buffer, int length, double reduceTo);