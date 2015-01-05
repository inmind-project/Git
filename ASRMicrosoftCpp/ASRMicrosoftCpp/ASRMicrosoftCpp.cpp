// ASRMicrosoftCpp.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "ASRwrapper.h"
#include "InMind_Server_MicrosoftASR.h"
#include "Utils.h"

using namespace std;


//int _tmain(int argc, _TCHAR* argv[])

//std::wstring Java_To_WStr(JNIEnv *env, jstring string)
//{
//	std::wstring value;
//
//	const jchar *raw = env->GetStringChars(string, 0);
//	jsize len = env->GetStringLength(string);
//	const jchar *temp = raw;
//
//	value.assign(raw, raw + len);
//
//	env->ReleaseStringChars(string, raw);
//
//	return value;
//}

/**
* Created by Amos Azaria on 31-Dec-14.
*/
JNIEXPORT jstring JNICALL Java_InMind_Server_MicrosoftASR_fromByteArr(JNIEnv *env, jclass, jbyteArray jbyteJArr, jdouble jreduceFactor)
{
	jboolean isCopy;
	jbyte* jbytePtr = env->GetByteArrayElements(jbyteJArr, &isCopy);
	jsize jarrSize = env->GetArrayLength(jbyteJArr);


	char* arrRec = (char*)jbytePtr;
	long arrSize = (long)jarrSize;
	double reduceFactor = (double)jreduceFactor;
	std::string sretRes = "";
	if (arrRec != NULL && arrSize > 0)
	{
		ReduceAmplitude(arrRec, arrSize, jreduceFactor);
		std::wstring speechRes = CASRwrapper::DecodeFromCharArr(arrRec, arrSize); // (*env)->GetStringUTFChars(env, string, 0); ////Java_To_WStr(env, sPathToFile);

		std::string sspeachRes(speechRes.begin(), speechRes.end()); //converting from wstring to string
		sretRes = sspeachRes;

		cout << sspeachRes << endl;

		env->ReleaseByteArrayElements(jbyteJArr, jbytePtr, JNI_ABORT);
	}
	return env->NewStringUTF(sretRes.c_str());
}

