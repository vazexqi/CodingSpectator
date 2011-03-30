#!/usr/bin/ruby1.9.1
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

require 'tmpdir'
require 'fileutils'

update_site_root = ARGV[0]
widen_patch_ranges_xsl = ARGV[1]
content_jar = "#{update_site_root}/content.jar"

raise "Could not locate the file \"#{content_jar}\"." unless File.exists?(content_jar)
raise "Could not locate the file \"#{widen_patch_ranges_xsl}\"." unless File.exists?(widen_patch_ranges_xsl)

Dir.mktmpdir { |tmp_dir_name|
  FileUtils.copy(content_jar, "#{tmp_dir_name}/content.original.jar")
  FileUtils.copy(widen_patch_ranges_xsl, "#{tmp_dir_name}/widen_patch_ranges.xsl")
  puts("Extracting #{tmp_dir_name}/content.original.jar ...")
  `cd #{tmp_dir_name}; jar xf content.original.jar`
  FileUtils.copy("#{tmp_dir_name}/content.xml", "#{tmp_dir_name}/content.original.xml")
  puts("Widening the patch ranges in #{tmp_dir_name}/content.original.xml ...")
  `cd #{tmp_dir_name}; xsltproc -o content.xml widen_patch_ranges.xsl content.original.xml`
  puts("Creating #{tmp_dir_name}/content.jar ...")
  `cd #{tmp_dir_name}; jar cf content.jar content.xml`
  FileUtils.cp("#{tmp_dir_name}/content.jar", content_jar)
}

