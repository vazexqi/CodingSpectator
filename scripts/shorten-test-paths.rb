#!/usr/bin/ruby1.9.1
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
#author: Mohsen Vakilian
# This script shortens the length of the paths in edu.illinois.codingspectator.ui.tests.
# This script renames paths of the first form to the second one.
# 1. /edu.illinois.codingspectator.ui.tests/expected-descriptors/<refactoring-name>/<long-test-name>/((performed|eclipse)/)?<long-test-project-name>
# 2. /edu.illinois.codingspectator.ui.tests/expected-descriptors/<refactoring-name>/Tdd/((performed|eclipse)/)?Prj
# Then, the script updates the project names in the "refactorings.history" files.

require 'fileutils'

def subfolders(path)
   Dir.entries(path) - [".", ".."]
end

def enclosing_folder_path(path)
   File.split(File.expand_path(path)).first
end

def replace_in_file(path, from, to)
  file_contents = File.read(path)
  new_file_contents = file_contents.gsub(/#{from}/, "#{to}")
  File.open(path, 'w') { |file| file.write(new_file_contents) }
end

def rename_java_test_class(src_path, test_name, new_test_name)
   java_file_path = Dir["#{src_path}/#{$ui_tests_plugin_name.gsub('.', '/')}/*/#{test_name}.java"].first
   raise "Could not fine the Java class file for #{test_name}" if java_file_path.nil? 
   replace_in_file(java_file_path, test_name, new_test_name)
   FileUtils.mv(java_file_path, "#{enclosing_folder_path(java_file_path)}/#{new_test_name}.java")
end

$ui_tests_plugin_name = "edu.illinois.codingspectator.ui.tests"
raise "Expected the path to the #{$ui_tests_plugin_name} plug-in." unless ARGV.size == 1
$ui_tests_plugin_path = ARGV.first

raise "Could not find the plug-in \"#{$ui_tests_plugin_name}\"." unless $ui_tests_plugin_name.eql?(File.basename(File.expand_path($ui_tests_plugin_path)))

$expected_descriptors_folder_name = "expected-descriptors"
$expected_descriptors_path = "#{$ui_tests_plugin_path}/#{$expected_descriptors_folder_name}"

puts "We renamed the names of the tests because of issue #180 as follows:\n"

subfolders($expected_descriptors_path).each do |refactoring_folder_name|
  puts "- #{refactoring_folder_name}"
  refactoring_path = "#{$expected_descriptors_path}/#{refactoring_folder_name}"
  subfolders(refactoring_path).each_with_index do |refactoring_test_folder_name, test_number|
    refactoring_test_path = "#{refactoring_path}/#{refactoring_test_folder_name}"
    new_refactoring_test_folder_name = sprintf("T%02d", test_number + 1)
    puts "    - #{new_refactoring_test_folder_name}, #{refactoring_test_folder_name}"
    rename_java_test_class("#{$ui_tests_plugin_path}/src", refactoring_test_folder_name, new_refactoring_test_folder_name)
    new_refactoring_test_path = "#{refactoring_path}/#{new_refactoring_test_folder_name}"
    FileUtils.mv(refactoring_test_path, new_refactoring_test_path)

    subfolders(new_refactoring_test_path).each do |refactoring_test_subfolder_name|
      project_name = refactoring_test_subfolder_name
      project_parent_path = new_refactoring_test_path
      project_path = "#{new_refactoring_test_path}/#{project_name}"
      if (["performed", "eclipse"].include?(refactoring_test_subfolder_name)) then
        project_name = subfolders(project_path).first
        project_parent_path = project_path
        project_path = "#{project_parent_path}/#{project_name}"
      end
      new_project_name = "Prj"
      refactoring_history_path_pattern = "#{project_path}/*/*/*/refactorings.history"
      refactoring_history_path = Dir[refactoring_history_path_pattern].first
      raise "Could not find the refactoring history file at #{refactoring_history_path_pattern}" if refactoring_history_path.nil?
      replace_in_file(refactoring_history_path, project_name, new_project_name)
      new_project_path = "#{project_parent_path}/#{new_project_name}"
      FileUtils.mv(project_path, new_project_path)
    end
  end
end

