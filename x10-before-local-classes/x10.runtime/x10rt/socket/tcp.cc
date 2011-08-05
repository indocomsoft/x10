#include "TCP.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <errno.h>
#include <assert.h>

#ifdef __CYGWIN__
extern "C" int snprintf(char *, size_t, const char *, ...);
#endif

/* ****************************************************************** */
/* ****************************************************************** */

int TCP::read(int fd, void * p, unsigned cnt)
{
	char * dst = (char *) p;
	unsigned bytesleft = cnt;

	while (bytesleft > 0)
	{
		int rc = ::read(fd, dst, bytesleft);
		if (rc == -1) /* !!!! read interrupted */
		{
			if (errno == EINTR) continue;
			return -1;
		}
		if (rc == 0)
		{
			return -1; /* connection closed */
		}

		dst += rc;
		bytesleft -= rc;
	}
	return cnt;
}

/* ****************************************************************** */
/* ****************************************************************** */

int TCP::write(int fd, void * p, unsigned cnt)
{
	char * src = (char *) p;
	unsigned bytesleft = cnt;

	while (bytesleft > 0)
	{
		int rc = ::write(fd, src, bytesleft);
		if (rc == -1) /* !!!! read interrupted */
		{
			if (errno == EINTR) continue;
			return -1;
		}
		if (rc == 0)
			return -1; /* connection closed */
		src += rc;
		bytesleft -= rc;
	}
	return cnt;
}

/* ****************************************************************** */
/* ****************************************************************** */

int TCP::listen(unsigned * localPort, unsigned backlog)
{
	int fd = socket(AF_INET, SOCK_STREAM, 0);
	if (fd == -1)
		FATAL("Socket creation failed");

	/* prevent "bind: Address already in use" errors */

	int reuse = 1;
	if (setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse)) < 0)
		FATAL("Socket option set failed");

	/* bind */

	sockaddr_in la;
	la.sin_family = AF_INET;
	la.sin_addr.s_addr = htonl(INADDR_ANY);
	la.sin_port = htons((unsigned short) (*localPort));
	if (::bind(fd, (sockaddr *) &la, sizeof(la)) == -1)
	{
		close(fd);
		FATAL("Bind failed");
	}

	/* listen */

	if (::listen(fd, backlog) == -1)
	{
		close(fd);
		FATAL("Listen failed");
	}

	sockaddr_in lb;
	socklen_t sl;
	if (::getsockname(fd, (sockaddr *) &lb, &sl) == -1)
	{
		close(fd);
		FATAL("getsockname failed");
	}

	// fprintf (stderr, "localport=%d\n", ntohs(lb.sin_port));

	*localPort = (unsigned) (ntohs(lb.sin_port));

	return fd;
}

/* ****************************************************************** */
/* ****************************************************************** */

int TCP::accept(int fd)
{
	int connFD;
	sockaddr_in remoteAddress;
	socklen_t len = sizeof(remoteAddress);

	for (;;)
	{
		connFD = ::accept(fd, (sockaddr *) &remoteAddress, &len);
		if (connFD != -1) break;
		if (errno != EINTR)
			FATAL("accept failed");
	}

	assert(len == sizeof(remoteAddress));
	assert(remoteAddress.sin_family == AF_INET);

	int ndelay = 1;
	if (setsockopt(connFD, IPPROTO_TCP, TCP_NODELAY, &ndelay, sizeof(ndelay)) < 0)
		FATAL("Nodelay option not set");

	return connFD;
}

/* ****************************************************************** */
/* ****************************************************************** */

int TCP::connect(const char *host, unsigned port, unsigned retries)
{
	int rc;
	hostent *remoteInfo = gethostbyname(host);
	if (!remoteInfo) FATAL("cannot resolve remote hostname");

	assert(remoteInfo->h_addrtype == AF_INET);
	assert(remoteInfo->h_length == sizeof(unsigned));

	fprintf(stderr, "host = %s, port = %i\n", host, port);

	sockaddr_in ra;
	memset(&ra, 0, sizeof(ra));
	ra.sin_family = AF_INET;
	ra.sin_addr.s_addr = *(unsigned *) remoteInfo->h_addr_list[0];
	ra.sin_port = htons(port);

	int connectionFd;
	for (unsigned retry = 0;;)
	{
		connectionFd = ::socket(AF_INET, SOCK_STREAM, 0);
		if (connectionFd == -1)
			FATAL("TCP::connect cannot create socket");
		rc = ::connect(connectionFd, (sockaddr *) &ra, sizeof(ra));
		if (rc == 0) break;

		close(connectionFd);
		if (retry++ >= retries)
			FATAL("TCP::connect timeout");
		sleep(1);
	}

	int enable = 1;
	rc = setsockopt(connectionFd, IPPROTO_TCP, TCP_NODELAY, &enable, sizeof(enable));
	if (rc < 0)
		FATAL("Cannot set socket options on fd");
	return connectionFd;
}

/* ****************************************************************** */
/* ****************************************************************** */

int TCP::connect(const char * hostport, unsigned retries)
{
	char hostport2[1000];
	strcpy(hostport2, hostport);
	char * c = strchr(hostport2, ':');
	if (c == NULL) FATAL("Malformed host:port");
	c[0] = '\0';
	return connect(hostport2, atoi(c + 1), retries);
}

/* ****************************************************************** */
/* ****************************************************************** */

void TCP::FATAL(const char * msg)
{
	fprintf(stderr, "FATAL Error: ");
	perror(msg);
	exit(-1);
}

/* ****************************************************************** */
/*                   get a description of the socket                  */
/* ****************************************************************** */

int TCP::getname(int fd, char * name, unsigned namelen)
{
	sockaddr_in addr;
/*	unsigned ipaddress;

	{
		char hostname[100];
		if (gethostname(hostname, sizeof(hostname)))
			FATAL("gethostname");
		hostent *info = gethostbyname(hostname);
		if (!info)
			FATAL("Error getting network info (likely a bad \"hosts\" file)");
		assert(info->h_addrtype == AF_INET);
		assert(info->h_length == sizeof(unsigned));
		ipaddress = htonl(*(unsigned *) info->h_addr_list[0]);
	}
*/
	{
		socklen_t len = sizeof(addr);
		if (getsockname(fd, (sockaddr *) &addr, &len) < 0)
			return -1;
	}

/*	snprintf(name, namelen, "%u.%u.%u.%u:%u", (ipaddress >> 24) & 0xff,
			(ipaddress >> 16) & 0xff, (ipaddress >> 8) & 0xff, (ipaddress >> 0)
					& 0xff, ntohs(addr.sin_port));
*/
	if (gethostname(name, namelen-10) == -1)
		FATAL("gethostname");
	int lenofName = strlen(name);
	snprintf(&name[lenofName], namelen-lenofName, ":%u", ntohs(addr.sin_port));
	return 0;
}
