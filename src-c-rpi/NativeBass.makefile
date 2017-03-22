NAME := NativeBass
ODIR = obj
RELEASE_BIN_DIR = Release
DEBUG_BIN_DIR = Debug
RELEASE_OBJ_DIR = $(ODIR)/Release
DEBUG_OBJ_DIR = $(ODIR)/Debug
RELEASE_TARGET := $(RELEASE_BIN_DIR)/lib$(NAME).so
DEBUG_TARGET := $(DEBUG_BIN_DIR)/lib$(NAME).so
CC = g++

RELEASEOPTIMIZE = -O
DEBUGOPTIMIZE = -O0
DEBUG = -g -D DEBUG -D _DEBUG

CFLAGS+=-I/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include
CFLAGS+=-I/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux
CFLAGS+=-DNATIVE2JAVA_TARGET_LINUX
CFLAGS+=-Wall -std=c++11 -shared -fPIC -pthread -fno-stack-protector
RELEASECFLAGS = $(CFLAGS) $(RELEASEOPTIMIZE) -o $@ 
DEBUGCFLAGS = $(CFLAGS) $(DEBUGOPTIMIZE) $(DEBUG) -o $@ 
LFLAGS+=-lrt -lpthread -Wl,-rpath=.

all: prep release

#include the appropriate dependency files

#if no command line args, we're building release
ifndef $(MAKECMDGOALS)
-include obj/Release/*.d
else
ifeq ($(MAKECMDGOALS),release)
-include obj/Release/*.d
else ifeq ($(MAKECMDGOALS), )
-include obj/Release/*.d
else ifeq ($(MAKECMDGOALS),debug)
-include obj/Debug/*.d
endif
endif

release: CFLAGS += $(RELEASEOPTIMIZE) -o $@
release: LFLAGS += -L$(RELEASE_BIN_DIR) -lbass 
release: OBJ_DIR += $(RELEASE_OBJ_DIR)
release: $(RELEASE_TARGET)

debug: CFLAGS += $(DEBUGOPTIMIZE) $(DEBUG) -o $@ 
debug: LFLAGS += -L$(DEBUG_BIN_DIR) -lbass 
debug: OBJ_DIR += $(DEBUG_OBJ_DIR)
debug: prep $(DEBUG_TARGET)


COMPILE_CMD = $(CC) $(CFLAGS) $< -c

SOURCEDIR = .

SRCFILES =  $(SRCS)

VPATH = $(SOURCEDIR):$(COMMONDIR)

SRCS := $(wildcard $(SOURCEDIR)/*.cpp)
_OBJ_FILES = $(notdir $(SRCS:.cpp=.o))

RELEASE_OBJFILES = $(patsubst %,$(RELEASE_OBJ_DIR)/%,$(_OBJ_FILES))
DEBUG_OBJFILES = $(patsubst %,$(DEBUG_OBJ_DIR)/%,$(_OBJ_FILES))

# destination path macro we'll use below
df = $(OBJ_DIR)/$(*F)


$(RELEASE_OBJ_DIR)/%.o: %.cpp
	@# Build the dependency file
	@$(CC) -MM -MP -MT $(df).o -MT $(df).d $(CFLAGS) $< > $(df).d
	@rm $(df).d
	@cp $(df).o $(df).d
	$(COMPILE_CMD)

$(DEBUG_OBJ_DIR)/%.o: %.cpp
	@# Build the dependency file
	@$(CC) -MM -MP -MT $(df).o -MT $(df).d $(CFLAGS) $< > $(df).d
	@rm $(df).d
	@cp $(df).o $(df).d
	$(COMPILE_CMD)


.PHONY: all prep clean debug

prep:
	mkdir -p $(RELEASE_BIN_DIR)
	mkdir -p $(DEBUG_BIN_DIR)
	mkdir -p $(ODIR)
	mkdir -p $(RELEASE_OBJ_DIR)
	mkdir -p $(DEBUG_OBJ_DIR)


$(RELEASE_TARGET): $(RELEASE_OBJFILES)
	$(CC) -shared $(RELEASE_OBJFILES) $(LFLAGS) -o "$@" $(LINKLIBS)

$(DEBUG_TARGET): $(DEBUG_OBJFILES)
	$(CC) -shared $(DEBUG_OBJFILES) $(LFLAGS) -o "$@" $(LINKLIBS)

clean:
	find . -type f -name "*.o" -delete
	$(RM) -r $(RELEASE_BIN_DIR)/lib$(NAME).so* $(DEBUG_BIN_DIR)/lib$(NAME).so* 
