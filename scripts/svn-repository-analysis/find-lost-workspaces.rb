#!/usr/bin/env ruby
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
#author: Mohsen Vakilian

require 'optparse'
require 'set'

$svn_repo_name = "CodingSpectatorData"
$svn_repo = "https://subversion.cs.illinois.edu/basic/#{$svn_repo_name}"

def parse_options
  $options = {}
  
  optparse = OptionParser.new do |opts|
    opts.banner = "Usage: svn log --username [username] --password [password] -v #{$svn_repo} | #{File.basename(__FILE__)} [options]"
  
    $options[:path_to_local_svn_repo] = nil
    opts.on("-p", "--path path", "path to the local working copy of the #{$svn_repo_name} Subversion repository") do |path|
      $options[:path] = path
    end
  
    opts.on('-h', '--help', 'Display this help screen') do
      puts opts
      exit
    end
  
  end
  
  optparse.parse!
  
  if $options[:path].nil? then
    puts "path is required."
    puts optparse.help
    exit
  end

end

def find_all_workspaces(svn_log)
  workspace_paths = Set.new
  pattern = /\s{3}.\s\/(?<username>cs-\d{3})\/(?<workspace_id>.{36})/
  svn_log.each_line do |line|
    matched = pattern.match(line)
    if matched then
      username = matched[:username]
      workspace_id = matched[:workspace_id]
      workspace_paths.add("#{username}/#{workspace_id}")
    end
  end
  workspace_paths
end

def find_missing_workspaces(workspace_paths)
  missing_workspaces = Set.new
  workspace_paths.each do |workspace_path|
    if not File.exists?("#{$options[:path]}/#{workspace_path}") then
      missing_workspaces.add(workspace_path)
    end
  end
  missing_workspaces
end

parse_options

workspace_paths = find_all_workspaces(ARGF.read)
missing_workspaces = find_missing_workspaces(workspace_paths)
missing_workspaces.each { |workspace_path| puts workspace_path }

