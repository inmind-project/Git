#include "stdafx.h"

//#define BUFFER_SIZE 4096
#define BUFFER_SIZE 4096

void usage(void);
std::wstring RunStreamingServer(int portNum);
std::wstring DecodeFromMem(IStream * pMemStream);
