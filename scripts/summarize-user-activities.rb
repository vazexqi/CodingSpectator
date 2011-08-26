#!/usr/bin/env ruby
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
#author: Mohsen Vakilian
#This scripts compiles the following information about each participant in the CSV format.
#1. the data of the creation of the participant's account
#2. the date of the last data submission of the participant
#3. the latest version of CodingSpectator that has submitted data from the participant's workspace

require 'optparse'

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

class SVNLog

  attr_reader :date, :username

  def initialize(svn_log_string)
    svn_log_elements = svn_log_string.split('|')
    @date = svn_log_elements[2][1.."YYYY-MM-DD".length]
    @username = svn_log_elements[1].strip
  end

end

class Participant

  attr_reader :last_data_submission_date, :account_creation_date, :version, :codingtracker_data_size

  def initialize(username)
    @username = username
  end

  def a_summer_2011_participant?
    not (@username =~ /cs-\d\d\d/).nil?
  end

  def summarize_activities
    find_latest_submission_information
    find_latest_codingspectator_version
    compute_codingtracker_data_size

    $stderr.print "."
  end

  def find_latest_submission_information
    svn_log = `svn log #{$svn_repo}/#{@username} #{svn_authentication_arguments} | grep "|"`
    svn_logs = svn_log.split("\n")
    svn_logs = svn_logs.map {|svn_log_string| SVNLog.new(svn_log_string)}

    participant_last_commit = svn_logs.find {|svn_log| Participant.new(svn_log.username).a_summer_2011_participant?}

    if participant_last_commit.nil?
      participant_last_commit_date = ""
    else
      participant_last_commit_date = participant_last_commit.date
    end

    @account_creation_date = svn_logs.last.date
    @last_data_submission_date = participant_last_commit_date
  end

  def svn_ls(path_relative_to_username_folder)
    svn_ls = `svn ls #{$svn_repo}/#{@username}/#{path_relative_to_username_folder} #{svn_authentication_arguments}`
    svn_ls_results = svn_ls.split("\n").map {|svn_ls_result| svn_ls_result[0, svn_ls_result.length - 1]}
  end

  def find_latest_codingspectator_version
    workspace_ids = svn_ls("")
    versions = workspace_ids.inject([""]) {|versions, workspace_id| versions += svn_ls(workspace_id)}
    @version = versions.max
  end

  def compute_codingtracker_data_size
    workspace_ids = svn_ls("")
    workspace_id_versions = workspace_ids.map {|workspace_id| svn_ls(workspace_id).map {|version| workspace_id + "/" + version}}.flatten
    codingtracker_log_sizes = workspace_id_versions.map {|workspace_id_version| svn_ls(workspace_id_version + "/codingtracker/codechanges.txt -v").map {|svn_ls_result| svn_ls_result.split(" ")[2].to_i}}.flatten
    @codingtracker_data_size = codingtracker_log_sizes.inject(0) {|codingtracker_total_log_size, codingtracker_log_size| codingtracker_total_log_size + codingtracker_log_size}
  end

  def to_s
    @username + "," + @account_creation_date + "," + @last_data_submission_date + "," + @version + "," + @codingtracker_data_size.to_s
  end

  private :find_latest_submission_information, :svn_ls, :find_latest_codingspectator_version, :compute_codingtracker_data_size

end

class Participants
  
  def initialize
    @participants = []
  end

  def add(participant)
    if participant.a_summer_2011_participant?
      participant.summarize_activities
      @participants = @participants + [participant] 
    end
  end

  def self.find_summer_2011_participants
    participants = Participants.new
    svn_ls_output = `svn ls #{$svn_repo} --username #{$options[:username]} --password #{$options[:password]}`
    usernames = svn_ls_output.split("/\n")
    usernames.each do |username|
      participants.add(Participant.new(username))
    end
    participants
  end

  def to_s
    csv_header = "username,account creation date (YYYY-MM-DD),last data submission date (YYYY-MM-DD),latest CodingSpectator version,CodingTracker data size (Bytes)\n"
    all_participants_string = @participants.inject("") {|participants_string, participant| participants_string += participant.to_s + "\n"}
    csv_header + all_participants_string
  end

end

parse_options
participants = Participants.find_summer_2011_participants

puts participants
$stderr.puts "\n\nWARNING: Please clear the history of your shell for security reasons."

