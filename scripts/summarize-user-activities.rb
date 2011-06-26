#!/usr/bin/ruby1.9.1
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
#author: Mohsen Vakilian
#This script lists the last data submission and CodingSpectator account creation dates of the participants in the CSV format.

require 'optparse'

$svn_repo_name = "CodingSpectatorData"
$svn_repo = "https://subversion.cs.illinois.edu/svn/#{$svn_repo_name}"

def parse_options
  $options = {}
  
  optparse = OptionParser.new do |opts|
    opts.banner = "Usage: #{File.basename(__FILE__)} [options]"
  
    $options[:username] = nil
    opts.on("-c", "--username username", "username for accessing the #{$svn_repo_name} subversion repository") do |username|
      $options[:username] = username
    end
  
  
    $options[:password] = nil
    opts.on("-c", "--password password", "password for accessing the #{$svn_repo_name} subversion repository") do |password|
      $options[:password] = password
    end

    opts.on('-h', '--help', 'Display this screen') do
      puts opts
      exit
    end
  
  end
  
  optparse.parse!
  
  raise OptionParser::MissingArgument if $options[:username].nil?
  raise OptionParser::MissingArgument if $options[:password].nil?

end

class SVNLog

  attr_reader :date

  def initialize(svn_log_string)
    svn_log_elements = svn_log_string.split('|')
    @date = svn_log_elements[2][1.."YYYY-MM-DD".length]
  end

end

class Participant

  attr_reader :last_data_submission_date, :account_creation_date

  def initialize(username)
    @username = username
  end

  def a_summer_2011_participant?
    not (@username =~ /cs-\d\d\d/).nil?
  end

  def find_account_activities
    svn_log = `svn log #{$svn_repo}/#{@username} --username #{$options[:username]} --password #{$options[:password]} | grep "|"`
    svn_logs = svn_log.split("\n")
    svn_logs = svn_logs.map {|svn_log_string| SVNLog.new(svn_log_string)}
    
    last_commit_date = svn_logs.first.date
    first_commit_date = svn_logs.last.date
    
    @account_creation_date = first_commit_date

    if last_commit_date != first_commit_date 
      @last_data_submission_date = last_commit_date
    else
      @last_data_submission_date = ""
    end

    $stderr.print "."
  end

  def to_s
    @username + "," + @account_creation_date + "," + @last_data_submission_date
  end

end

class Participants
  
  def initialize
    @participants = []
  end

  def add(participant)
    if participant.a_summer_2011_participant?
      participant.find_account_activities
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
    csv_header = "username,account creation date (YYYY-MM-DD),last data submission date (YYYY-MM-DD)\n"
    all_participants_string = @participants.inject("") {|participants_string, participant| participants_string += participant.to_s + "\n"}
    csv_header + all_participants_string
  end

end

parse_options
participants = Participants.find_summer_2011_participants

puts participants
$stderr.puts "\n\nWARNING: Please clear the history of your shell for security reasons."

