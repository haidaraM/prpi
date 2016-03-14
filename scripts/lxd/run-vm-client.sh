#!/bin/bash

CONTAINER_NAME="prpi-client"
SHARE_DIR="/tmp/prpi-client-IntelliJIdea15" # No slash at the end !
DEBUG_PORT=7533

. $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/run-vm.sh