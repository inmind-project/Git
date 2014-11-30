#include "stdafx.h"

#define BUFFER_SIZE 4096

void usage(void);
int RunStreamingServer(int argc, _TCHAR* argv[]);
int DecodeFromMem(IStream * pMemStream);