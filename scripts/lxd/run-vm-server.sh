#!/bin/bash

CONTAINER_NAME="prpi-server"
SHARE_DIR="/tmp/${CONTAINER_NAME}-IntelliJIdea15" # No slash at the end !
DEBUG_PORT=7534

. $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/run-vm.sh