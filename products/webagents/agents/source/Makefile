#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at
# https://opensso.dev.java.net/public/CDDLv1.0.html or
# opensso/legal/CDDLv1.0.txt
# See the License for the specific language governing
# permission and limitations under the License.
# 
# When distributing Covered Code, include this CDDL
# Header Notice in each file and include the License file
# at opensso/legal/CDDLv1.0.txt.
# If applicable, add the following below the CDDL Header,
# with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
#
# $Id: Makefile,v 1.17 2009-12-09 23:00:59 krishna_indigo Exp $
#
# Makefile file for building the Agents tree.
#
#
USERX_ROOT := ../..

include $(USERX_ROOT)/arch/components.mk

SRCS :=	

PACKAGE_PREFIX := $(CLASS_PREFIX_DIR)/dpro/agents

AGENT_COMPONENTS := \
	$(patsubst %.java, $(PACKAGE_PREFIX)/%.class, $(SRCS))

SUBDIRS := common

ifeq ($(BUILD_AGENT),)
ifeq ($(OS_ARCH), HP-UX)
SUBDIRS += apache apache22
else 
ifeq ($(OS_ARCH), AIX)
SUBDIRS += apache apache22
else 
BUILD_AGENT := apache sjsws apache22 proxy40
endif
endif
ifeq ($(OS_ARCH), WINNT)
BUILD_AGENT += iis7
endif
endif

ifeq ($(OS_ARCH), Linux)
SUBDIRS += $(BUILD_AGENT)
endif

ifeq ($(OS_ARCH), SunOS)
ifeq ($(MC_ARCH), i86pc)
SUBDIRS += $(BUILD_AGENT)
else
SUBDIRS += $(BUILD_AGENT)
endif
endif

ifeq ($(OS_ARCH), WINNT)
ifeq ($(CYGWIN_ARCH), WOW64)
SUBDIRS += iis7 iis6
else
SUBDIRS += $(BUILD_AGENT)
endif
endif

ifeq ($(OS_ARCH), HP-UX)
SUBDIRS += apache apache22
endif 

ifeq ($(OS_ARCH), AIX)
SUBDIRS += apache apache22 domino
endif 

all: all_subdirs

clean: clean_subdirs

include $(USERX_ROOT)/arch/rules.mk
