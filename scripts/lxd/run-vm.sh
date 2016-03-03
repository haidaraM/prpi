#!/bin/bash

printf "Check LXD ... "
if [ $(lxc list "${CONTAINER_NAME}" -c ns | grep "RUNNING" | wc -l) -ne 1 ]; then

    if [ $(lxc list "${CONTAINER_NAME}" -c n | grep " ${CONTAINER_NAME} " | wc -l) -ne 1 ]; then

        echo "No LXD ${CONTAINER_NAME} start or present, check with : ~$ lxc list" >&2
        exit 1

    else

        if [ ! -d "${SHARE_DIR}" ]; then
            mkdir ${SHARE_DIR}
        fi

        lxc start ${CONTAINER_NAME}

        if [ $? -ne 0 ]; then
            echo "Can't start LXD ${CONTAINER_NAME}, see error above." >&2
            exit 1
        fi

    fi
fi
echo "Ok"

printf "Sync data ... "
rsync -qr --delete-during ~/.IntelliJIdea15/ ${SHARE_DIR}/ >&2 2>/dev/null
echo "Ok"

printf "Set rules ... "
chmod a+rwx -R /tmp/${CONTAINER_NAME}-IntelliJIdea15 >&2 2>/dev/null
echo "Ok"

echo "Start IntelliJ IDEA ..."
echo "----------------------------------------------------------------------------"
ssh -X prpi@${CONTAINER_NAME} /opt/java/jdk1.8.0_66/bin/java \
 -Xmx512m -Xms256m -XX:MaxPermSize=250m -ea -Xbootclasspath/a:/opt/idea-IC/lib/boot.jar \
 -Didea.config.path=/home/prpi/.IntelliJIdea15/system/plugins-sandbox/config \
 -Didea.system.path=/home/prpi/.IntelliJIdea15/system/plugins-sandbox/system \
 -Didea.plugins.path=/home/prpi/.IntelliJIdea15/system/plugins-sandbox/plugins \
 -Didea.classpath.index.enabled=false \
 -Didea.required.plugins.id=com.prpi \
 -Dsun.awt.disablegrab=true \
 -Didea.platform.prefix=Idea \
 -Didea.launcher.port=${DEBUG_PORT} \
 -Didea.launcher.bin.path=/opt/idea-IC/bin \
 -Dfile.encoding=UTF-8 \
 -classpath /opt/java/jdk1.8.0_66/lib/tools.jar:/opt/idea-IC/lib/idea_rt.jar:/opt/idea-IC/lib/idea.jar:/opt/idea-IC/lib/bootstrap.jar:/opt/idea-IC/lib/extensions.jar:/opt/idea-IC/lib/util.jar:/opt/idea-IC/lib/openapi.jar:/opt/idea-IC/lib/trove4j.jar:/opt/idea-IC/lib/jdom.jar:/opt/idea-IC/lib/log4j.jar:/opt/idea-IC/lib/idea_rt.jar \
 com.intellij.rt.execution.application.AppMain \
 com.intellij.idea.Main
echo "----------------------------------------------------------------------------"
echo "Done."