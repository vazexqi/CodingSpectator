#!/usr/bin/env ruby
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

$THIS_FOLDER_PATH = File.expand_path(File.dirname(__FILE__))
$PARENT_OF_THIS_FOLDER_PATH = File.dirname($THIS_FOLDER_PATH)
system("#{$THIS_FOLDER_PATH}/patch/widen-patch-ranges.rb #{$PARENT_OF_THIS_FOLDER_PATH} #{$THIS_FOLDER_PATH}/patch/widen-patch-ranges.xsl")
system("#{$THIS_FOLDER_PATH}/sign/sign.rb #{$THIS_FOLDER_PATH}/sign/CodingSpectatorKeyStore")

