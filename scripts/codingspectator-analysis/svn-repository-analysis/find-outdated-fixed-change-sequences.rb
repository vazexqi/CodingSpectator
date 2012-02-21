#!/usr/bin/env ruby
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
#author: Mohsen Vakilian

require 'optparse'
require 'set'

$fixed_revision_file_names = [
  "#{ENV['CODINGSPECTATOR_SVN_FOLDER']}/Experiment/Summer2011/Internal/CodingTrackerPostprocessedLogs/fixed_revisions.csv",
  "#{ENV['CODINGSPECTATOR_SVN_FOLDER']}/Experiment/Summer2011/External/CodingTrackerPostprocessedLogs/fixed_revisions.csv"
]
$svn_repo_name = "CodingSpectatorData"
$svn_repo = "https://subversion.cs.illinois.edu/basic/#{$svn_repo_name}"

def parse_options
  $options = {}
  
  optparse = OptionParser.new do |opts|
    opts.banner = "Usage: #{File.basename(__FILE__)} [options]"
  
    $options[:username] = nil
    opts.on("-u", "--username username", "username for accessing the #{$svn_repo_name} subversion repository using the basic authentication mechanism (required)") do |username|
      $options[:username] = username
    end
  
  
    $options[:password] = nil
    opts.on("-p", "--password password", "password for accessing the #{$svn_repo_name} subversion repository using the basic authentication mechanism (required)") do |password|
      $options[:password] = password
    end

    opts.on('-h', '--help', 'Display this help screen') do
      puts opts
      exit
    end
  
  end
  
  optparse.parse!
  
  if $options[:username].nil? then
    puts "username is required."
    puts optparse.help
    exit
  end

  if $options[:password].nil? then
    puts "username is required."
    puts optparse.help
    exit
  end

end

def svn_authentication_arguments
  "--username #{$options[:username]} --password #{$options[:password]}"
end

class CodingTrackerSequence

  attr_reader :username, :workspace_id, :codingspectator_version, :fixed_revision

  def initialize(username, workspace_id, codingspectator_version, fixed_revision)
    @username = username
    @workspace_id = workspace_id
    @codingspectator_version = codingspectator_version
    @fixed_revision = fixed_revision
  end

  def eql?(other)
    @username.eql?(other.username) and @workspace_id.eql?(other.workspace_id) and @codingspectator_version.eql?(other.codingspectator_version) and @fixed_revision.eql?(other.fixed_revision)
  end

  def compute_last_changed_revision
    svn_info = `svn info #{svn_authentication_arguments} "#{$svn_repo}/#{relative_path}"`
    pattern = /Last Changed Rev: (?<last_changed_revision>\d+)/
    matched = pattern.match(svn_info)
    if matched then
      @last_changed_revision = matched[:last_changed_revision].to_i
    else
      raise "Could not find the last changed revision in the output of svn info."
    end
  end

  def get_last_changed_revision
    if not @last_changed_revision
      compute_last_changed_revision
    end
    @last_changed_revision
  end

  def hash
    91
  end

  def relative_path
    "#{@username}/#{@workspace_id}/#{@codingspectator_version}/codingtracker/codechanges.txt"
  end

  def to_s
    "\"#{relative_path}\",#{@fixed_revision},#{get_last_changed_revision}"
  end

  def has_outdated_fix?
    get_last_changed_revision > @fixed_revision
  end

end

def parse_fixed_revisions(fixed_revision_file_name)
  sequences = Set.new
  File.open(fixed_revision_file_name).each_line { |line|
    line = line.chomp
    if line != '"Fixed operation sequence file","list of used revisions"' then
      pattern = /"(?<username>cs-\d{3})\/(?<workspace_id>.*)\/(?<codingspectator_version>.*)\/codingtracker\/codechanges.txt\","(?<revision1>\d+)(, (?<revision2>\d+))?"/
      matched = pattern.match(line)
      if matched then
        fixed_revision = matched[:revision1].to_i
        if matched[:revision2] then
          fixed_revision = matched[:revision2].to_i
        end
        sequences.add(CodingTrackerSequence.new(matched[:username], matched[:workspace_id], matched[:codingspectator_version], fixed_revision))
      else
        raise "Could not parse #{line} in #{fixed_revision_file_name}."
      end
    end 
  }
  sequences
end

def parse_all_fixed_revisions
  $fixed_revision_file_names.inject(Set.new) {|result, file_name| result.union(parse_fixed_revisions(file_name))}
end

def find_all_outdated_fixed_sequences(sequences)
  sequences.find_all {|sequence| sequence.has_outdated_fix?}
end

def print_set(aset)
  aset.each do |element|
    puts element.to_s
  end
end

parse_options

if ENV['CODINGSPECTATOR_SVN_FOLDER'].nil? then
  raise "The enviornment variable CODINGSPECTATOR_SVN_FOLDER is not set."
end

puts "\"relative path of fixed CodingTracker sequence\",\"fixed revision\",\"last changed revision\""
print_set(find_all_outdated_fixed_sequences(parse_all_fixed_revisions))

