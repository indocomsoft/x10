#!/bin/bash

# Dave Grove

hosts="condor triloka3 nashira"

# TODO: we should get svn info by parsing svn info URL and extracting revision from there.
while [ $# != 0 ]; do
  case $1 in
    -rev)
        rev=$2
	shift
    ;;

    -hosts)
        hosts=$2
	shift
    ;;

   esac
   shift
done

if [[ -z "$rev" ]]; then
    echo "usage: $0 must give revision -rev <rev>"
    exit 1
fi

ssh orquesta.watson.ibm.com "mkdir -p /var/www/localhost/htdocs/x10dt/x10-builds/$rev"

for host in $hosts
do
    echo "Launching buildToolIntegration.sh on $host"
    scp buildToolIntegration.sh $host:/tmp 
    ssh $host "(cd /tmp; ./buildToolIntegration.sh -d /tmp/x10-tib-$USER -rev $rev)"
    ssh $host "(cd /tmp; rm ./buildToolIntegration.sh)"
    echo "transfering file from $host to localhost"
    scp "$host:/tmp/x10-tib-$USER/x10/x10.dist/x10-tib*.tgz" .
    echo "transfering from localhost to orquesta"
    scp x10-tib_*.tgz orquesta.watson.ibm.com:/var/www/localhost/htdocs/x10dt/x10-builds/$rev
    rm x10-tib_*.tgz
    #ssh $host rm -rf /tmp/x10-tib-$USER 
done
