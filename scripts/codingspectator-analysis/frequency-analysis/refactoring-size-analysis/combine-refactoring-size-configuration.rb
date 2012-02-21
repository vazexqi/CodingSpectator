#!/usr/bin/env ruby
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
#author: Mohsen Vakilian

require 'optparse'
require 'csv'

def parse_options
  $options = {}
  
  optparse = OptionParser.new do |opts|
    opts.banner = "Usage: #{File.basename(__FILE__)} [options]"
  
    $options[:size_csv_path] = nil
    opts.on("-s", "--size size_csv_path", "Path to the CSV file that reports the size of each automated refactoring.") do |size_csv_path|
      $options[:size_csv_path] = size_csv_path
    end
  
    $options[:configuration_csv_path] = nil
    opts.on("-c", "--configuration configuration_csv_path", "Path to the CSV file that reports the configuration time of each automated refactoring.") do |configuration_csv_path|
      $options[:configuration_csv_path] = configuration_csv_path
    end

    opts.on('-h', '--help', 'Display this help screen') do
      puts opts
      exit
    end
  
  end
  
  optparse.parse!
  
  if $options[:size_csv_path].nil? then
    puts "--size is required"
    puts optparse.help
    exit
  end

  if $options[:configuration_csv_path].nil? then
    puts "--configuration is required."
    puts optparse.help
    exit
  end

end

def csv_to_rows(csv_path)
  all_rows = CSV.read(csv_path)
  header_row = all_rows[0]
  data_rows = all_rows[1..-1]
  rows = data_rows.map{|data_row| CSV::Row.new(header_row, data_row)}
  rows
end

parse_options

size_csv_rows = csv_to_rows($options[:size_csv_path])
configuration_csv_rows = csv_to_rows($options[:configuration_csv_path])

output_csv_header = ["REFACTORING_ID", "USERNAME", "CONFIGURATION_TIMESTAMP", "SIZE_TIMESTAMP", "AFFECTED_FILES_COUNT", "AFFECTED_LINES_COUNT", "CONFIGURATION_TIME_IN_MILLI_SEC"]
puts output_csv_header.to_csv

configuration_csv_rows.each do |configuration_csv_row|
  matching_size_csv_row = size_csv_rows.find do |size_csv_row|
    configuration_csv_row["WORKSPACE_ID"] == size_csv_row["WORKSPACE_ID"] and
    configuration_csv_row["REFACTORING_ID"] == size_csv_row["REFACTORING_ID"] and
    (configuration_csv_row["TIMESTAMP"].to_i - size_csv_row["TIMESTAMP"].to_i).abs < 1000 #TODO: This matching is not good enough. Some refactorings are about 5 minutes apart.
  end

  if matching_size_csv_row then
    output_csv_row = [configuration_csv_row["REFACTORING_ID"], configuration_csv_row["USERNAME"], configuration_csv_row["TIMESTAMP"], matching_size_csv_row["TIMESTAMP"], matching_size_csv_row["AFFECTED_FILES_COUNT"], matching_size_csv_row["AFFECTED_LINES_COUNT"], configuration_csv_row["CONFIGURATION_TIME_IN_MILLI_SEC"]]
    puts output_csv_row.to_csv
  end
end

