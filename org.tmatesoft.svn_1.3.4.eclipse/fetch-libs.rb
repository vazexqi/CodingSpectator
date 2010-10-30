#!/usr/bin/ruby

zip_file="org.tmatesoft.svn_1.3.4.standalone.zip"
zip_top_dir="svnkit-1.3.4.6888"
files_to_copy=["antlr-runtime-3.1.3.jar", "jna.jar", "sqljet.1.0.3.jar",
"svnkit-cli.jar", "svnkit.jar", "svnkit-javahl.jar", "trilead.jar"]

`wget http://www.svnkit.com/#{zip_file}`
`sha1sum -c checksums`

if `echo $?`.chomp == "0"
  `unzip #{zip_file}`
  files_to_copy.each do |file|
    `cp #{File.join("#{zip_top_dir}", "#{file}")} .` 
  end
else
  puts "Unsuccessfull download of #{zip_file}. Please try again."
end

`rm #{zip_file} #{zip_top_dir} -rf`
