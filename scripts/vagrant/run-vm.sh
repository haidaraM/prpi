#!/usr/bin/env bash

# adresse ip de la vm
ADRESSE_IP=192.168.33.10 # j'utilise l'ip car j'ai plusieurs vms qui tournent souvent
VM_OPTION="-Xmx512m -Xms256m -XX:MaxPermSize=250m -ea"
DEBUG_PORT=7533

# la syncrhonisation des repertoires de compilaiton du plugin se fait directement dans le VagrantFile

ssh -X vagrant@${ADRESSE_IP} java \
 ${VM_OPTION} \
 -Xbootclasspath/a:/opt/idea-IC/lib/boot.jar \
 -Didea.config.path=/home/vagrant/.IntelliJIdea15/config \
 -Didea.system.path=/home/vagrant/.IntelliJIdea15/system \
 -Didea.plugins.path=/home/vagrant/.IntelliJIdea15/plugins \
 -Didea.classpath.index.enabled=false \
 -Didea.required.plugins.id=com.prpi \
 -Dsun.awt.disablegrab=true \
 -Didea.platform.prefix=Idea \
 -Didea.launcher.port=${DEBUG_PORT} \
 -Didea.launcher.bin.path=/home/vagrant/idea-IC-145.597.3/bin \
 -Dfile.encoding=UTF-8 \
 -classpath /usr/lib/jvm/java-8-oracle/lib/tools.jar:/home/vagrant/idea-IC-145.597.3/lib/idea_rt.jar:/home/vagrant/idea-IC-145.597.3/lib/idea.jar:/home/vagrant/idea-IC-145.597.3/lib/bootstrap.jar:/home/vagrant/idea-IC-145.597.3/lib/extensions.jar:/home/vagrant/idea-IC-145.597.3/lib/util.jar:/home/vagrant/idea-IC-145.597.3/lib/openapi.jar:/home/vagrant/idea-IC-145.597.3/lib/trove4j.jar:/home/vagrant/idea-IC-145.597.3/lib/jdom.jar:/home/vagrant/idea-IC-145.597.3/lib/log4j.jar:/home/vagrant/idea-IC-145.597.3/lib/idea_rt.jar \
 com.intellij.rt.execution.application.AppMain \
 com.intellij.idea.Main


