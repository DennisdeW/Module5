/*
 * Global.c
 *
 *  Created on: Oct 14, 2014
 *      Author: Dennis
 */

#include "Global.h"
#include "File.h"
#include <stdio.h>
#include <inttypes.h>

void printFD(FileDescriptor *fd) {
	printf("File %s:\n\towner=%"PRId64"\n\tsize=%"PRId64"",
			fd->identifier, fd->owner, fd->size);
}

int main(int argc, char**argv) {
	printf("LIBSSH2_VERSION_NUM: %x\nSQLITE_VERSION: %s\n", LIBSSH2_VERSION_NUM, SQLITE_VERSION);
	FileDescriptor fd;
	fd.identifier = "TestFile";
	fd.owner = 0xABCD;
	fd.size = 12345;
	printFD(&fd);
	return 0;
}

