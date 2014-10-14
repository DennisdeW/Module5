/*
 * Global.h
 *
 *  Created on: Oct 14, 2014
 *      Author: Dennis
 */

#ifndef SRC_GLOBAL_H_
#define SRC_GLOBAL_H_

#include "libssh2.h"
#include "sqlite3.h"

#define bool struct {True=1; False=0};

typedef unsigned long long ulong;
typedef unsigned int uint;
typedef ulong uid_t;
typedef char* bytearr;
typedef char* Password;

#endif /* SRC_GLOBAL_H_ */
