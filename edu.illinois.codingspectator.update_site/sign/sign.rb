#!/usr/bin/ruby

def sign(jar)
  `jarsigner -keystore #{$keystore} -storepass #{$password} -keypass #{$password} -signedjar #{signedCopy(jar)} #{jar} CodingSpectatorAlias`
  `mv #{signedCopy(jar)} #{jar}`
end

def signedCopy(jar)
  File.join(File.dirname(jar), "signed-#{File.basename(jar)}")
end

puts ARGV[0]
$keystore = ARGV[0]
$keystore = "CodingSpectatorKeyStore" if $keystore.nil?
print "Enter keystore password:"
$password = $stdin.gets.chomp

Dir.glob("**/*.jar") do |jar|
  puts jar
  sign(jar)
end

