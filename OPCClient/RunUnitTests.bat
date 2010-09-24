@echo off
title = unit test runner

%1 --gtest_output=xml

pause

exit