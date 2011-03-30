#!/usr/bin/ruby1.9.1
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

$update_site_root = ARGV[0]
$widen_patch_ranges_xsl = ARGV[1]
$content_jar = "#{$update_site_root}/content.jar"

raise "Could not locate the file \"#{$content_jar}\"." unless File.exists?($content_jar)
raise "Could not locate the file \"#{$widen_patch_ranges_xsl}\"." unless File.exists?($widen_patch_ranges_xsl)

`jar xf #{$content_jar}`
`mv content.xml content.original.xml`
`xsltproc -o content.xml #{$widen_patch_ranges_xsl} content.original.xml`
`jar cf content-with-widened-patch-ranges.jar content.xml`
`mv content-with-widened-patch-ranges.jar #{$content_jar}`
`rm content.xml content.original.xml`

