#include "stdafx.h"

//#define BUFFER_SIZE 4096
#define BUFFER_SIZE 4096

void usage(void);
int RunStreamingServer(int argc, _TCHAR* argv[]);
int DecodeFromMem(IStream * pMemStream);

static void PutNum(long num, void *writeTo, int endianness, int bytes, bool isFile, int offset);
void WriteWavHeader(void *writeTo, int SampleRate, bool isFile);
void CloseWav(void *writeTo, bool isFile, int totLengthBytes);
void WriteToFile(char* data, char* filePath, int dataBytes);
