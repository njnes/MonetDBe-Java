/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class nl_cwi_monetdb_monetdbe_MonetNative */

#ifndef _Included_nl_cwi_monetdb_monetdbe_MonetNative
#define _Included_nl_cwi_monetdb_monetdbe_MonetNative
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_open
 * Signature: (Ljava/lang/String;)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1open__Ljava_lang_String_2
  (JNIEnv *, jclass, jstring);

/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_open
 * Signature: (Ljava/lang/String;IIII)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1open__Ljava_lang_String_2IIII
  (JNIEnv *, jclass, jstring, jint, jint, jint, jint);

/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_close
 * Signature: (Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1close
  (JNIEnv *, jclass, jobject);

/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_query
 * Signature: (Ljava/nio/ByteBuffer;Ljava/lang/String;Lnl/cwi/monetdb/monetdbe/MonetStatement;)Lnl/cwi/monetdb/monetdbe/MonetResultSet;
 */
JNIEXPORT jobject JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1query
  (JNIEnv *, jclass, jobject, jstring, jobject);

/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_result_fetch_all
 * Signature: (Ljava/nio/ByteBuffer;II)[Lnl/cwi/monetdb/monetdbe/MonetColumn;
 */
JNIEXPORT jobjectArray JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1result_1fetch_1all
  (JNIEnv *, jclass, jobject, jint, jint);

/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_result_cleanup
 * Signature: (Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1result_1cleanup
  (JNIEnv *, jclass, jobject, jobject);

/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_error
 * Signature: (Ljava/nio/ByteBuffer;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1error
  (JNIEnv *, jclass, jobject);

/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_set_autocommit
 * Signature: (Ljava/nio/ByteBuffer;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1set_1autocommit
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     nl_cwi_monetdb_monetdbe_MonetNative
 * Method:    monetdbe_get_autocommit
 * Signature: (Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jboolean JNICALL Java_nl_cwi_monetdb_monetdbe_MonetNative_monetdbe_1get_1autocommit
  (JNIEnv *, jclass, jobject);

#ifdef __cplusplus
}
#endif
#endif
