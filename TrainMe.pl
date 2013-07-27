#!/usr/bin/perl
use utf8;
system("java -Xmx3072m -Dfile.encoding=UTF-8 -jar NERTrainer.jar TrainingList.full ModelDump.ser");
