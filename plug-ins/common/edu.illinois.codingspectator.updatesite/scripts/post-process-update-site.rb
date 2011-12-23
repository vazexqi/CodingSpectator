#!/usr/bin/env ruby
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

require 'optparse'

$THIS_FOLDER_PATH = File.expand_path(File.dirname(__FILE__))
$PARENT_OF_THIS_FOLDER_PATH = File.dirname($THIS_FOLDER_PATH)

def parse_options
  $options = {}

  optparse = OptionParser.new do |opts|
    opts.banner = "Usage: #{File.basename(__FILE__)} [options]"

    $options[:release] = nil
    opts.on('-r', '--eclipse-release release', 'Name of the Eclipse release to prepare feature patches for (helios, indigo, or indigo-sr1)') do |release|
      $options[:release] = release
    end

   opts.on('-h', '--help', 'Display this screen') do
      puts opts
      exit
    end

  end

  optparse.parse!

  abort "Unspecified Eclipse release." unless $options[:release]

end

parse_options
system("#{$THIS_FOLDER_PATH}/patch/widen-patch-ranges.rb #{$PARENT_OF_THIS_FOLDER_PATH} #{$THIS_FOLDER_PATH}/patch/widen-patch-ranges-#{$options[:release]}.xsl")
system("#{$THIS_FOLDER_PATH}/sign/sign.rb #{$THIS_FOLDER_PATH}/sign/CodingSpectatorKeyStore")

