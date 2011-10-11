v1.0.0.201110111329 (Helios) and v1.0.0.201110111338 (Indigo)
=============================================================
- Improve the reliability and performance of SWTBot tests by using wait instead of sleep commands (issue [#226](https://github.com/vazexqi/CodingSpectator/issues/226)).
- Assumed that recording compilation problems doesn't change user experience (issue [#211](https://github.com/vazexqi/CodingSpectator/issues/211)).
- Added the instructions for setting up the secure storage mechanism of Eclipse on different platforms (issue [#265](https://github.com/vazexqi/CodingSpectator/issues/265)).
- Confirmed that CodingTracker works fine with the refresh on access feature of Eclipse Indigo (issue [#266](https://github.com/vazexqi/CodingSpectator/issues/266)).
- Wrote several SQL queries to analyze the collected data and answer our research questions (issue [#288](https://github.com/vazexqi/CodingSpectator/issues/288)).
- Developed an analyzer to generate a CSV report of refactorings followed by runs of programs and tests (issue [#293](https://github.com/vazexqi/CodingSpectator/issues/293)).
- Modified a script to report the latest version of CodingSpectator each participant has used to submit data (issue [#294](https://github.com/vazexqi/CodingSpectator/issues/294)).
- Modified a script to report the amount of data CodingTracker has collected from each participant (issue [#316](https://github.com/vazexqi/CodingSpectator/issues/316)).
- Considered the committer in Subversion logs when finding the latest data submission of each participant (issue [#296](https://github.com/vazexqi/CodingSpectator/issues/296)).
- Fixed the parser of refactoring histories to correctly deserialize the refactorings captured by systems with different locales (issue [#298](https://github.com/vazexqi/CodingSpectator/issues/298)).
- Assumed that Subversion's complaints about files that have been added to the working copy but do not exist are because the participants have manipulated the working copy (issue [#299](https://github.com/vazexqi/CodingSpectator/issues/299)).
- Added a SQL query to compare the usage frequencies of refactorings reported by UDC and CodingSpectator (issue [#303](https://github.com/vazexqi/CodingSpectator/issues/303)).
- Decided not to conduct the awareness survey online (issue [#315](https://github.com/vazexqi/CodingSpectator/issues/315)).
- Marked parts of the captured code snippets that the participants had selected to invoke refactorings (issue [#317](https://github.com/vazexqi/CodingSpectator/issues/317)).
- Fixed a bug in the `logstocsv` program that truncated the `comments` attribute of refactorings (issue [#320](https://github.com/vazexqi/CodingSpectator/issues/320)).
- Wrote a script to combine the CSV reports of the size and configuration time of refactorings (issue [#322](https://github.com/vazexqi/CodingSpectator/issues/322)).
- Changed the maximum length of the first line of commit messages to 72 characters (issue [#325](https://github.com/vazexqi/CodingSpectator/issues/325)).
- Captured invocations of the Infer Generic Type Arguments refactoring (issue [#272](https://github.com/vazexqi/CodingSpectator/issues/272)).
- Buffered the CodingSpectator data outside the watched folder and copied it over before submissions (issue [#287](https://github.com/vazexqi/CodingSpectator/issues/287)).
- Moved two SVN operations from Submitter#authenticateAndInitialize to Submitter#submit (issue [#308](https://github.com/vazexqi/CodingSpectator/issues/308)).
- Made the submitter upload data even when the watched folder is outdated or has conflicts (issues [#257](https://github.com/vazexqi/CodingSpectator/issues/257), [#309](https://github.com/vazexqi/CodingSpectator/issues/309), and [#311](https://github.com/vazexqi/CodingSpectator/issues/311)).
- Used an instance of Eclipse without CodingSpectator to resolve the issue with old `SubmitterListener`s (issue [#326](https://github.com/vazexqi/CodingSpectator/issues/326)).
- Increased the versions of two JDT bundles to make CodingSpectator compatible with Eclipse Indigo SR1 (issues [#327](https://github.com/vazexqi/CodingSpectator/issues/327), [#328](https://github.com/vazexqi/CodingSpectator/issues/328)).
- Implemented usage time analyzer (issue [#321](https://github.com/vazexqi/CodingSpectator/issues/321)) and refactoring effects analyzer.
- Improved the replay speed of CodingTracker, also added the 'jump to' action (issue [#314](https://github.com/vazexqi/CodingSpectator/issues/314)).
- Added postprocessor to fix the negative refactoring timestamps and updated all the broken sequences (issue [#301](https://github.com/vazexqi/CodingSpectator/issues/301)).
- Implemented an automated converter from the old to the new API of refactoring operations and updated all the old sequences correspondingly in order to simplify the supported API (issue [#300](https://github.com/vazexqi/CodingSpectator/issues/300)).
- Implemented a postprocessor that fixes spurious SVN commit operations (issue [#241](https://github.com/vazexqi/CodingSpectator/issues/241)).
- Sped up registering of CodingTracker's listeners (issue [#324](https://github.com/vazexqi/CodingSpectator/issues/324)).
- Updated older CodingTracker sequences to the new data format and committed them to the internal repository (issue [#244](https://github.com/vazexqi/CodingSpectator/issues/244)).
- Investigated `NoSuchMethodError` thrown from `org.eclipse.compare.core` and established that it is not caused by CodingTracker (issue [#253](https://github.com/vazexqi/CodingSpectator/issues/253)).
- Implemented support for conflict editors opened via 'Compare With' menu (issue [#286](https://github.com/vazexqi/CodingSpectator/issues/286)).
- Manually fixed and committed to the local repository a sequence containing conflict markers (issue [#291](https://github.com/vazexqi/CodingSpectator/issues/291)).
- Explained to @reprogrammer what is `FinishedRefactoringOperation.success` (issue [#297](https://github.com/vazexqi/CodingSpectator/issues/297)).
- Extended the functionality of the 'Find' replay action to look for an operation with the closest timestamp if a precise match cannot be found (issue [#313](https://github.com/vazexqi/CodingSpectator/issues/313)).

v1.0.0.201107172332 (Helios) and v1.0.0.201107172337 (Indigo)
=============================================================
- Logged the encapsulate field refactoring (issues [#270](https://github.com/vazexqi/CodingSpectator/issues/270), [#275](https://github.com/vazexqi/CodingSpectator/issues/275), [#277](https://github.com/vazexqi/CodingSpectator/issues/277)).
- Avoided `Display.syncExec` in activator and removed other unnecessary invocations of `Display.syncExec` (issues [#276](https://github.com/vazexqi/CodingSpectator/issues/276), [#281](https://github.com/vazexqi/CodingSpectator/issues/281)).
- Made the submitter tolerate the failure of its extensions (issue [#282](https://github.com/vazexqi/CodingSpectator/issues/282)).
- Made the submitter create a single instance for each of its extensions (issue [#283](https://github.com/vazexqi/CodingSpectator/issues/283)).
- Made the bundle updater handle NPE's better (issue [#284](https://github.com/vazexqi/CodingSpectator/issues/284)).
- Copied the UDC data into the watched folder even if the user has disabled the upload to Eclipse foundation (issue [#285](https://github.com/vazexqi/CodingSpectator/issues/285)).
- Made the ongoing submission capture the existing Eclipse refactoring logs and UDC data (issues [#218](https://github.com/vazexqi/CodingSpectator/issues/218), [#280](https://github.com/vazexqi/CodingSpectator/issues/280)).
- CodingTracker compares Charsets rather than their textual representations when it checks that a file is known (issue [#267](https://github.com/vazexqi/CodingSpectator/issues/267)).

v1.0.0.201107061725 (Helios) and v1.0.0.201107061737 (Indigo)
=============================================================
- Automated exporting some of the data (parts of refactoring descriptors and relevant CodingTracker events) into a CSV file (issues [#234](https://github.com/vazexqi/CodingSpectator/issues/234), [#235](https://github.com/vazexqi/CodingSpectator/issues/235), [#243](https://github.com/vazexqi/CodingSpectator/issues/243))
- Automated importing the CSV data file into a relational database (issue [#254](https://github.com/vazexqi/CodingSpectator/issues/254)).
- Updated the FAQ section of the homepage (issues [#246](https://github.com/vazexqi/CodingSpectator/issues/246), [#250](https://github.com/vazexqi/CodingSpectator/issues/250)).
- Added an acknowledgement section to the homepage (issue [#249](https://github.com/vazexqi/CodingSpectator/issues/249)).
- Informed users about the presence of CodingSpectator by a small icon in the status line (issue [#251](https://github.com/vazexqi/CodingSpectator/issues/251)).
- Reminded users about new versions of CodingSpectator after every data submission (issue [#245](https://github.com/vazexqi/CodingSpectator/issues/245)).
- Made CodingSpectator collect UDC data from Eclipse SDK (issue [#214](https://github.com/vazexqi/CodingSpectator/issues/214)).
- Supported Eclipse Indigo (issue [#206](https://github.com/vazexqi/CodingSpectator/issues/206)).
- Disabled data submission from target platforms (issue [#252](https://github.com/vazexqi/CodingSpectator/issues/252)).
- Updated the user guide by the new branding features of CodingSpectator and the support for Indigo (issue [#259](https://github.com/vazexqi/CodingSpectator/issues/259)).
- Fixed NPE due to saving of inexisting conflict editor as well as ensured that no duplicated code changes are recorded (issue [#247](https://github.com/vazexqi/CodingSpectator/issues/247)).
- Ensured to some degree of confidence that recording of compilation problems does not affect user experience (issue [#211](https://github.com/vazexqi/CodingSpectator/issues/211)).

v1.0.0.201105300951
===================
- Computed the selection information immediately after the selection is made (issues [#232](https://github.com/vazexqi/CodingSpectator/issues/232), [#240](https://github.com/vazexqi/CodingSpectator/issues/240)).
- Fixed a bug in capturing refactorings on non-Java elements (issues [#231](https://github.com/vazexqi/CodingSpectator/issues/231), [#239](https://github.com/vazexqi/CodingSpectator/issues/239)).
- Converted some of CodingSpectator logs into a CSV file (issues [#230](https://github.com/vazexqi/CodingSpectator/issues/230), [#233](https://github.com/vazexqi/CodingSpectator/issues/233)).
- Fixed the timestamps of about to perform refactorings in CodingTracker (issue [#237](https://github.com/vazexqi/CodingSpectator/issues/237)).

v1.0.0.201105242245
===================
- Improved the selection information captured in refactoring descriptors (issues [#163](https://github.com/vazexqi/CodingSpectator/issues/163), [#170](https://github.com/vazexqi/CodingSpectator/issues/170), [#207](https://github.com/vazexqi/CodingSpectator/issues/207), [#216](https://github.com/vazexqi/CodingSpectator/issues/216), [#195](https://github.com/vazexqi/CodingSpectator/issues/195)).
- Captured compilation problems due to refactorings (issues [#194](https://github.com/vazexqi/CodingSpectator/issues/194), [#200](https://github.com/vazexqi/CodingSpectator/issues/200), [#210](https://github.com/vazexqi/CodingSpectator/issues/210)).
- Fixed a bug that could potentially cause CodingSpectator miss UDC data (issue [#214](https://github.com/vazexqi/CodingSpectator/issues/214)).
- Improved the installation instructions of the user guide (issues [#209](https://github.com/vazexqi/CodingSpectator/issues/209), [#217](https://github.com/vazexqi/CodingSpectator/issues/217)).
- Made the generated timestamps more consistent (issue [#120](https://github.com/vazexqi/CodingSpectator/issues/120)).
- Made CodingSpectator parse the contents of refactoring logs into generic refactoring descriptors (issue [#202](https://github.com/vazexqi/CodingSpectator/issues/202)).
- Added more content and menu items to the home page of CodingSpectator (issue [#228](https://github.com/vazexqi/CodingSpectator/issues/228)).
- Fixed a bug that caused duplicate button presses to be recorded in the `navigation-history` attribute (issue [#227](https://github.com/vazexqi/CodingSpectator/issues/227)).
- Added the ability to automatically generate and overwrite expected logs of the UI tests (issue [#225](https://github.com/vazexqi/CodingSpectator/issues/225)).
- Added tests for the rest of refactorings that subclass `WatchedJavaRefactoring` (issue [#222](https://github.com/vazexqi/CodingSpectator/issues/222)).
- Stored the right `ITypeRoot` object in the global store and used it for computing code snippets (issue [#220](https://github.com/vazexqi/CodingSpectator/issues/220), [#223](https://github.com/vazexqi/CodingSpectator/issues/223), [#224](https://github.com/vazexqi/CodingSpectator/issues/224), [#154](https://github.com/vazexqi/CodingSpectator/issues/154), [#22](https://github.com/vazexqi/CodingSpectator/issues/22)).
- Captured the change method signature refactoring (issue [#204](https://github.com/vazexqi/CodingSpectator/issues/204)).
- Captured the introduce parameter object refactoring (issue [#32](https://github.com/vazexqi/CodingSpectator/issues/32)).
- Implemented recording and replaying of atomic changes produced by refactorings (issues [#176](https://github.com/vazexqi/CodingSpectator/issues/176) and [#203](https://github.com/vazexqi/CodingSpectator/issues/203)).
- Added tracking of affected compilation units that are in refactored packages (issue [#219](https://github.com/vazexqi/CodingSpectator/issues/209)).

v1.0.0.201104162211
===================
- Enabled the update notification mechanism of Eclipse for every new workspace ([issue #173](https://github.com/vazexqi/CodingSpectator/issues/173)).
- Shortened the path lengths of the test plug-in ([issue #180](https://github.com/vazexqi/CodingSpectator/issues/180)).
- Enforced a limit on the lengths of the paths of the repository ([issue #182](https://github.com/vazexqi/CodingSpectator/issues/182).
- Included the name of the test when reporting an error in automated tests ([issue #183](https://github.com/vazexqi/CodingSpectator/issues/183)).
- Fixed the failure of some of the reorg tests of JDT ([issue #179](https://github.com/vazexqi/CodingSpectator/issues/179)).
- Captured the changes to compilation problems due to refactorings ([issue #191](https://github.com/vazexqi/CodingSpectator/issues/191)).
- Verified the contents of the descriptors of the remaining refactorings ([issue #138](https://github.com/vazexqi/CodingSpectator/issues/138)).
- Added the remaining tests for the move refactoring ([issue #124](https://github.com/vazexqi/CodingSpectator/issues/124)).
- Made the patches of Eclipse features more robust ([issue #188](https://github.com/vazexqi/CodingSpectator/issues/188)).
- Implemented replaying of compare editor operations ([issue #177](https://github.com/vazexqi/CodingSpectator/issues/177)).
- Fixed encoding problems ([issue #177](https://github.com/vazexqi/CodingSpectator/issues/177)).

v1.0.0.201103210128
===================
- Instrumented the use super type refactoring ([issue #158](https://github.com/vazexqi/CodingSpectator/issues/158)).
- Instrumented the extract interface refactoring (issues [#157](https://github.com/vazexqi/CodingSpectator/issues/157) and [#160](https://github.com/vazexqi/CodingSpectator/issues/160)).
- Extracted the saferecorder plugin from CodingTracker to safely record data while a submission is in progress ([issue #149](https://github.com/vazexqi/CodingSpectator/issues/149)).
- Captured the Eclipse error log ([issue #143](https://github.com/vazexqi/CodingSpectator/issues/143)).
- Captured more information about selections in some of the refactorings ([issue #163](https://github.com/vazexqi/CodingSpectator/issues/163)).
- Made CodingSpectator support Eclipse 3.6.2 (Helios SR2) ([issue #161](https://github.com/vazexqi/CodingSpectator/issues/161)).
- Changed the URL of the repository to the one for users outside the university ([issue #159](https://github.com/vazexqi/CodingSpectator/issues/159)).
- Captured the events for running JUnit tests and applications ([issue #140](https://github.com/vazexqi/CodingSpectator/issues/140)).
- Captured unavailable inline local variable refactorings (issues [#30](https://github.com/vazexqi/CodingSpectator/issues/30) and [#150](https://github.com/vazexqi/CodingSpectator/issues/150)).
- Captured unavailable inline constant refactorings ([issues #27](https://github.com/vazexqi/CodingSpectator/issues/27)).
- Captured the LTK history of refactorings ([issue #167](https://github.com/vazexqi/CodingSpectator/issues/167)).
- Moved the watched directory of CodingSpectator from ".metadata/.plugins/org.eclipse.ltk.core.refactoring" to ".metadata/.plugins/edu.illinois.codingspectator.data" ([issue #166](https://github.com/vazexqi/CodingSpectator/issues/166)).
- Made "edu.illinois.codingspectator.data" provide the version number of CodingSpectator ([issue #63](https://github.com/vazexqi/CodingSpectator/issues/63)).
- Fixed the tests for the rename type refactoring ([issue #164](https://github.com/vazexqi/CodingSpectator/issues/164)).
- Activated the hyperlink to the CodingSpectator web page on the authentication dialog ([issue #172](https://github.com/vazexqi/CodingSpectator/issues/172)).
- Extracted a feature for CodingTracker from that of CodingSpectator ([issue #178](https://github.com/vazexqi/CodingSpectator/issues/178)).
- Restored back the "selection" attribute that had been missing from some of the refactorings whose classes extend `WatchedJavaRefactoring` ([issue #168](https://github.com/vazexqi/CodingSpectator/issues/168)).
- Improved the replay functionality of CodingTracker ([issue #165](https://github.com/vazexqi/CodingSpectator/issues/165)).
- Added support for CVS in CodingTracker ([issue #151](https://github.com/vazexqi/CodingSpectator/issues/151)).
- Fixed the descriptor of the extract method refactoring for replaying ([issue #169](https://github.com/vazexqi/CodingSpectator/issues/169)).
- Verified the contents of the descriptors of some of the refactorings ([issue #138](https://github.com/vazexqi/CodingSpectator/issues/138)).
- Captured more unavailable inline method refactorings ([issue #18](https://github.com/vazexqi/CodingSpectator/issues/18)).
- Added more tests for the move refactoring ([issue #124](https://github.com/vazexqi/CodingSpectator/issues/124)).

v1.0.0.201102192319
===================
- Captured the navigation history in a refactoring wizard. The navigation history includes the time the user presses a navigation button such as "OK", "Next", "Cancel", "Preview" and "Finish" on a page of a refactoring wizard ([issue #125](https://github.com/vazexqi/CodingSpectator/issues/125)).
- Instrumented the pull up refactoring to capture performed, cancelled and unavailable invocations ([issue #22](https://github.com/vazexqi/CodingSpectator/issues/22)).
- Instrumented the extract superclass refactoring to capture performed, cancelled and unavailable invocations ([issue #44](https://github.com/vazexqi/CodingSpectator/issues/44)).
- Fixed instances of [LCK08-J](https://www.securecoding.cert.org/confluence/display/java/LCK08-J.+Ensure+actively+held+locks+are+released+on+exceptional+conditions) in the instrumentation of UDC ([issue #139](https://github.com/vazexqi/CodingSpectator/issues/139)).
- Updated the user interface messages for users outside the university ([issue #155](https://github.com/vazexqi/CodingSpectator/issues/155)).
- Provided the user's guide in PDF and HTML formats, and updated it for users outside the university ([issue #147](https://github.com/vazexqi/CodingSpectator/issues/147)).
- Instrumented the push down refactoring to capture performed, cancelled and unavailable invocations ([issue #153](https://github.com/vazexqi/CodingSpectator/issues/153)).
- Added tests for all kinds of the rename refactoring ([issue #124](https://github.com/vazexqi/CodingSpectator/issues/124)).
- Reorganized the tests by separating the tests for performed refactorings from those for cancelled ones ([issue #141](https://github.com/vazexqi/CodingSpectator/issues/141)).

v1.0.0.201101161822
===================
- Added the ability to clean up the submission working directory ([issue #113](https://github.com/vazexqi/CodingSpectator/issues/113)).
- Split `SVNManager` into `LocalSVNManager` and `RemoteSVNManager` ([issue #113](https://github.com/vazexqi/CodingSpectator/issues/113)).
- Changed the SVN repository to assembla when running in test or debug mode ([issue #105](https://github.com/vazexqi/CodingSpectator/issues/105)).
- Disabled the check box for turning off the UDC capture.
- Turned on UDC capture by default for all workspaces. And, let the user choose to submit UDC data to CodingSpectator but not the Eclipse server.
- Made CodingSpectator collect the UDC data even though it doesn't get uploaded to the Eclipse server ([issue #55](https://github.com/vazexqi/CodingSpectator/issues/55)).
- Prevented CodingSpectator from uploading data while Eclipse is about to shut down ([issue #121](https://github.com/vazexqi/CodingSpectator/issues/121)).
- Fixed the `UnsupportedOperationException` in the instrumentation of the rename compilation unit refactoring ([issue #118](https://github.com/vazexqi/CodingSpectator/issues/118)).
- Made `MoverProcessor` and `RenameProcessor` use delegation instead of inner classes ([issue #118](https://github.com/vazexqi/CodingSpectator/issues/118)).
- Forced the menu item for extract method refactoring to be enabled in test mode in order to make SWTBot happy ([issue #81](https://github.com/vazexqi/CodingSpectator/issues/81)).
- Added automated UI tests for the rename compilation unit refactoring ([issue #122](https://github.com/vazexqi/CodingSpectator/issues/122)).
- Wrote automated UI tests for the following refactorings: move static field, move static method, move instance method, move compilation unit ([issue #124](https://github.com/vazexqi/CodingSpectator/issues/124)).
- Self-signed the jar files ([issue #133](https://github.com/vazexqi/CodingSpectator/issues/133)).

v1.0.0.201011241815
===================
- Copied UDC data to the watched directory of CodingSpectator just before UDC uploads its data.

v1.0.0.201011201702
===================
- Captured failures of refactorings before the refactoring wizard. These early failures make the refactorings unavailable. (See [issue #30](https://github.com/vazexqi/CodingSpectator/issues/30) for more details).
- Captured refactorings that programmers perform from quick assist (See [issue #109](https://github.com/vazexqi/CodingSpectator/issues/109) for more details).

v1.0.0.201011161607
===================
- Fixed a bug that logged a refactoring as both performed and canceled (See [issue #110](https://github.com/vazexqi/CodingSpectator/issues/110) for more details).

v1.0.0.201011111502
===================
- Added the support for the move refactoring.
- Adapted the automated refactoring tests.

v1.0.0.201011031917
===================
- Supported the rename refactoring.

v1.0.0.201011022017
===================
- Made it possible to disable the startup event in the target platform.

v1.0.0.201010211411
===================
- Patched org.eclipse.compare.
- Improved secure storage of authentication information.

v1.0.0.201010181311
===================
- Made the feature patches optional to coexist with other plugins.

v1.0.0.201010151736
===================
- Added CodingTracker to CodingSpectator.
- Added an extension to listen to pre and post submit events.

v1.0.0.201010071957
===================
- Added the support for the following refactorings: extract constant, extract method, extract local variable, inline constant, inline method, inline local variable, introduce factory, convert local variable to field.

