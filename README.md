## DESCRIPTION

The Simian Army is a suite of tools for keeping your cloud operating in top form.  Chaos Monkey, the first member, is a resiliency tool that
helps ensure that your applications can tolerate random instance failures

## FORK

In this fork support of containers was added to make simian army framework be able to connect containers via Docker API and run daemons inside. Unfortunately, frameworks that was used by Netflix and design that was implemented required a lot of hacks, so this wasn't used in the real system and I would encouradge one to write the same functionality from scratch, if you need Docker support.

## Travis CI build status
[![Build Status](https://travis-ci.org/grammarly/SimianArmy.svg)](https://travis-ci.org/grammarly/SimianArmy)

## DETAILS

Please see the [wiki](https://github.com/Netflix/SimianArmy/wiki).

## SUPPORT

If you have any questions please email me at stanislav [dot] levental @ gmail [dot] com

## LICENSE

Copyright 2012 Netflix, Inc.

Licensed under the Apache License, Version 2.0 (the “License”); you may not use this file except in
compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is
distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing permissions and limitations under the
License.
