###############################################################################
# Copyright (c) 2007 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Hisashi MIYASHITA - initial API and implementation
###############################################################################

BASEPKG = org.eclipse.actf.ai.screenreader.jaws
JAVAH = "$(JAVA_HOME:/=\)\bin\javah"
JAVAH_FLAGS = -classpath ../bin
JAVA_INC = "$(JAVA_HOME)/include"
BLD = ..\build
CC = cl.exe
CFLAGS = -c -Zi -Od -I $(JAVA_INC)
LINK = link.exe
LDFLAGS = -debug -dll
LIBS = User32.lib AdvAPI32.lib

OBJS = $(BLD)\jawsapi-bridge.obj $(BLD)\IEWindow.obj $(BLD)\JawsWindow.obj
DLL = ../jawsapi-bridge.dll

all: init headers $(DLL)

init:
	-mkdir $(BLD)

clean:
	-rd /S/Q $(BLD)

headers: 
	$(JAVAH) $(JAVAH_FLAGS) $(BASEPKG).JawsAPI $(BASEPKG).JawsWindowUtil

$(DLL): $(OBJS)
	$(LINK) $(LDFLAGS) $(OBJS) -out:$(DLL) $(LIBS)

.cpp{$(BLD)}.obj:
	$(CC) $(CFLAGS) -Fo$(BLD)\ $<


