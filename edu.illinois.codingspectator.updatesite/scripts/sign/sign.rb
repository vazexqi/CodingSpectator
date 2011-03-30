#!/usr/bin/ruby1.9.1
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

def sign(jar)
  `jarsigner -keystore #{$keystore} -storepass #{$password} -keypass #{$password} -signedjar #{signedCopy(jar)} #{jar} CodingSpectatorAlias`
  `mv #{signedCopy(jar)} #{jar}`
end

def signedCopy(jar)
  File.join(File.dirname(jar), "signed-#{File.basename(jar)}")
end

$keystore = ARGV[0]
$keystore = "CodingSpectatorKeyStore" if $keystore.nil?

if not File.exists?($keystore)
   puts "Could not find the file \"#{$keystore}\"."
   exit
end

print "Enter keystore password:"
$password = $stdin.gets.chomp

Dir.glob("**/*.jar") do |jar|
  puts "Signing #{jar}..."
  sign(jar)
end

