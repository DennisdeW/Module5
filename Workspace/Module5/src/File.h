//#include "../libssh2.lib"
/*
 * File.h
 *
 *  Created on: Oct 14, 2014
 *      Author: Dennis
 */
#include "Global.h"

#ifndef SRC_FILE_H_
#define SRC_FILE_H_

typedef char* HashStr;

typedef struct {
	char *identifier;
	ulong size;
	uid_t owner;
} FileDescriptor;

#endif /* SRC_FILE_H_ */
