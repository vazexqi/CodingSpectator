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

