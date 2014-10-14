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

struct FileDescriptor {
	char *identifier;
	ulong size;
	ulong userId;
};

#endif /* SRC_FILE_H_ */
