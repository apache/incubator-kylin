---
layout: docs
title:  How to making a release
categories: howto
permalink: /docs/howto/howto_release.html
version: v0.7.2
since: v0.7.1
---

_This guide is for Apache Kylin Committers only._  
_Shell commands is on Mac OS X as sample._  
_For people in China, please aware using proxy to avoid potential firewall issue._  

## Setup Account  
Make sure you have avaliable account and privlidge for following applications:

* Apache account: [https://id.apache.org](https://id.apache.org/)    
* Apache Kylin git repo (main cobe base): [https://git-wip-us.apache.org/repos/asf/incubator-kylin.git](https://git-wip-us.apache.org/repos/asf/incubator-kylin.git)  
* Apache Kylin svn repo (for website only): [https://svn.apache.org/repos/asf/incubator/kylin](https://svn.apache.org/repos/asf/incubator/kylin)  
* Apache Nexus (maven repo): [https://repository.apache.org](https://repository.apache.org)  
* Apache Kylin dist repo: [https://dist.apache.org/repos/dist/dev/incubator/kylin](https://dist.apache.org/repos/dist/dev/incubator/kylin)  

## Setup PGP signing keys  
Follow instructions at [http://www.apache.org/dev/release-signing](http://www.apache.org/dev/release-signing) to create a key pair  
Install gpg (On Mac OS X as sample):  
`brew install gpg and gpg --gen-key`

Generate gpg key:  
Reference: [https://www.gnupg.org/gph/en/manual/c14.html](https://www.gnupg.org/gph/en/manual/c14.html)  
_All new RSA keys generated should be at least 4096 bits. Do not generate new DSA keys_  
`gpg --gen-key`  

Verify your key:  
`gpg --list-sigs YOUR_NAME`

Then add your key to your apache account, for example:  
[https://people.apache.org/keys/committer/lukehan.asc](https://people.apache.org/keys/committer/lukehan.asc)  
Generate ASCII Amromed Key:  
`gpg -a --export YOUR_MAIL_ADDRESS > YOUR_NAME.asc &`

Upload key to public server:  
`gpg --send-keys YOUR_KEY_HASH`

or Submit key via web:  
Open and Submit to [http://pool.sks-keyservers.net:11371](http://pool.sks-keyservers.net:11371) (you can pickup any avaliable public key server)  
Once your key submitted to server, you can verify using following command:  
`gpg --recv-keys YOUR_KEY_HASH`

for example:  
`gpg --recv-keys 027DC364`

Add your public key to the KEYS file by following instructions in the KEYS file.:  
_KEYS file location:_ __${incubator-kylin}/KEYS__  
For example:  
`(gpg --list-sigs YOURNAME && gpg --armor --export YOURNAME) >> KEYS`

Commit your changes.

## Prepare artifacts for release  
__Before you start:__

* Set up signing keys as described above.
* Make sure you are using JDK 1.7 (not 1.8).
* Make sure you are working on right release version number.
* Make sure that every “resolved” JIRA case (including duplicates) has a fix version assigned.

__Verify licenses__  
Run Apache RAT to check licenses issue:  
{% highlight bash %}
mvn -Papache-release clean rat:rat
{% endhighlight %}

Fix license issue if any.

__Making a snapshot__  
{% highlight bash %}
# Set passphrase variable without putting it into shell history
$ read -s GPG_PASSPHRASE

# Make sure that there are no junk files in the sandbox
$ git clean -xn
$ mvn clean

$ mvn -Papache-release -Dgpg.passphrase=${GPG_PASSPHRASE} install
{% endhighlight %}
When the dry-run has succeeded, change install to deploy.

__Making a release__

Create a release branch named after the release, e.g. v0.7.2-release, and push it to Apache.  
{% highlight bash %}
$ git checkout -b vX.Y.Z-release
$ git push -u origin vX.Y.Z-release
{% endhighlight %}
We will use the branch for the entire the release process. Meanwhile, we do not allow commits to the master branch. After the release is final, we can use `git merge --ff-only` to append the changes on the release branch onto the master branch. (Apache does not allow reverts to the master branch, which makes it difficult to clean up the kind of messy commits that inevitably happen while you are trying to finalize a release.)

Now, set up your environment and do a dry run. The dry run will not commit any changes back to git and gives you the opportunity to verify that the release process will complete as expected.

If any of the steps fail, clean up (see below), fix the problem, and start again from the top.  
{% highlight bash %}
# Set passphrase variable without putting it into shell history
$ read -s GPG_PASSPHRASE

# Make sure that there are no junk files in the sandbox
$ git clean -xn
$ mvn clean

# Do a dry run of the release:prepare step, which sets version numbers.
$ mvn -DdryRun=true -DskipTests -DreleaseVersion=X.Y.Z-incubating -DdevelopmentVersion=X.Y.Z+1-incubating-SNAPSHOT -Papache-release -Darguments="-Dgpg.passphrase=${GPG_PASSPHRASE}" release:prepare 2>&1 | tee /tmp/prepare-dry.log
{% endhighlight %}

__Check the artifacts:__

* In the `target` directory should be these 8 files, among others:
  * apache-kylin-X.Y.Z-incubating-src.tar.gz
  * apache-kylin-X.Y.Z-incubating-src.tar.gz.asc
  * apache-kylin-X.Y.Z-incubating-src.tar.gz.md5
  * apache-kylin-X.Y.Z-incubating-src.tar.gz.sha1
  * apache-kylin-X.Y.Z-incubating-src.zip
  * apache-kylin-X.Y.Z-incubating-src.zip.asc
  * apache-kylin-X.Y.Z-incubating-src.zip.md5
  * apache-kylin-X.Y.Z-incubating-src.zip.sha1
* Note that the file names start `apache-kylin-` and include
  `incubating` in the version.
* In the two source distros `.tar.gz` and `.zip`, check that all files belong to a directory called
  `apache-kylin-X.Y.Z-incubating-src`.
* That directory must contain files `DISCLAIMER`, `NOTICE`, `LICENSE`, `README.md`
* Check PGP, per [this](https://httpd.apache.org/dev/verification.html)

__Run real release:__
Now, run the release for real.  
{% highlight bash %}
# Prepare sets the version numbers, creates a tag, and pushes it to git.
$ mvn -DskipTests -Papache-release -Darguments="-Dgpg.passphrase=${GPG_PASSPHRASE} -DskipTests" clean release:prepare

# Perform checks out the tagged version, builds, and deploys to the staging repository
$ mvn -DskipTests -Papache-release -Darguments="-Dgpg.passphrase=${GPG_PASSPHRASE} -DskipTests" release:perform
{% endhighlight %}

__Verify the staged artifacts in the Nexus repository:__  
* Go to [https://repository.apache.org/](https://repository.apache.org/) and login
* Under `Build Promotion`, click `Staging Repositories`
* In the `Staging Repositories` tab there should be a line with profile `org.apache.kylin`
* Navigate through the artifact tree and make sure the .jar, .pom, .asc files are present
* Check the box on in the first column of the row,
  and press the 'Close' button to publish the repository at
  https://repository.apache.org/content/repositories/orgapachekylin-1006
  (or a similar URL)

__Upload to staging area:__  
Upload the artifacts via subversion to a staging area, https://dist.apache.org/repos/dist/dev/incubator/kylin/apache-kylin-X.Y.Z-incubating-rcN:
{% highlight bash %}
# Create a subversion workspace, if you haven't already
$ mkdir -p ~/dist/dev
$ pushd ~/dist/dev
$ svn co https://dist.apache.org/repos/dist/dev/incubator/kylin
$ popd

## Move the files into a directory
$ cd target
$ mkdir ~/dist/dev/kylin/apache-kylin-X.Y.Z-incubating-rcN
$ mv apache-kylin-* ~/dist/dev/kylin/apache-kylin-X.Y.Z-incubating-rcN

## Check in
$ cd ~/dist/dev/kylin
$ svn add apache-kylin-X.Y.Z-incubating-rcN
$ svn commit -m 'Upload release artifacts to staging'
{% endhighlight %}

__Cleaning up after a failed release attempt:__
{% highlight bash %}
# Make sure that the tag you are about to generate does not already
# exist (due to a failed release attempt)
$ git tag

# If the tag exists, delete it locally and remotely
$ git tag -d apache-kylin-X.Y.Z-incubating
$ git push origin :refs/tags/apache-kylin-X.Y.Z-incubating

# Remove modified files
$ mvn release:clean

# Check whether there are modified files and if so, go back to the
# original git commit
$ git status
$ git reset --hard HEAD
{% endhighlight %}

# Validate a release
{% highlight bash %}
# Check unit test
$ mvn test

# Check that the signing key (e.g. 2AD3FAE3) is pushed
$ gpg --recv-keys key

# Check keys
$ curl -O https://dist.apache.org/repos/dist/release/incubator/kylin/KEYS

## Sign/check md5 and sha1 hashes
 _(Assumes your O/S has 'md5' and 'sha1' commands.)_
function checkHash() {
  cd "$1"
  for i in *.{zip,gz}; do
    if [ ! -f $i ]; then
      continue
    fi
    if [ -f $i.md5 ]; then
      if [ "$(cat $i.md5)" = "$(md5 -q $i)" ]; then
        echo $i.md5 present and correct
      else
        echo $i.md5 does not match
      fi
    else
      md5 -q $i > $i.md5
      echo $i.md5 created
    fi
    if [ -f $i.sha1 ]; then
      if [ "$(cat $i.sha1)" = "$(sha1 -q $i)" ]; then
        echo $i.sha1 present and correct
      else
        echo $i.sha1 does not match
      fi
    else
      sha1 -q $i > $i.sha1
      echo $i.sha1 created
    fi
  done
}
$ checkHash apache-kylin-X.Y.Z-incubating-rcN
{% endhighlight %}

## Apache voting process  

__Vote on Apache Kylin dev mailing list__  
Release vote on dev list:  

{% highlight text %}
To: dev@kylin.incubator.apache.org
Subject: [VOTE] Release apache-kylin-X.Y.Z-incubating (release candidate N)

Hi all,

I have created a build for Apache Kylin X.Y.Z-incubating, release candidate N.

Changes highlights:
...

Thanks to everyone who has contributed to this release.
Here’s release notes:
https://github.com/apache/incubator-kylin/blob/XXX/docs/release_notes.md

The commit to be voted upon:

https://github.com/apache/incubator-kylin/commit/xxx

Its hash is xxx.

The artifacts to be voted on are located here:
https://dist.apache.org/repos/dist/dev/incubator/kylin/apache-kylin-X.Y.Z-incubating-rcN/

The hashes of the artifacts are as follows:
src.zip.md5 xxx
src.zip.sha1 xxx
src.tar.gz.md5 xxx
src.tar.gz.sha1 xxx

A staged Maven repository is available for review at:
https://repository.apache.org/content/repositories/orgapachekylin-XXXX/

Release artifacts are signed with the following key:
https://people.apache.org/keys/committer/lukehan.asc

Please vote on releasing this package as Apache Kylin X.Y.Z-incubating.

The vote is open for the next 72 hours and passes if a majority of
at least three +1 PPMC votes are cast.

[ ] +1 Release this package as Apache Kylin X.Y.Z-incubating
[ ]  0 I don't feel strongly about it, but I'm okay with the release
[ ] -1 Do not release this package because...


Here is my vote:

+1 (binding)

Luke

{% endhighlight %}

After vote finishes, send out the result:  
{% highlight text %}
Subject: [RESULT] [VOTE] Release apache-kylin-X.Y.Z-incubating (release candidate N)
To: dev@kylin.incubator.apache.org

Thanks to everyone who has tested the release candidate and given
their comments and votes.

The tally is as follows.

N binding +1s:

N non-binding +1s:

No 0s or -1s.

Therefore I am delighted to announce that the proposal to release
Apache-Kylin-X.Y.Z-incubating has passed.

I'll now start a vote on the general list. Those of you in the IPMC,
please recast your vote on the new thread.

Luke

{% endhighlight %}

__Vote on Apache incubator general mailing list__   
Use the [Apache URL shortener](http://s.apache.org) to generate
shortened URLs for the vote proposal and result emails. Examples:
[http://s.apache.org/kylin-0.7.1-vote_rc3](http://s.apache.org/kylin-0.7.1-vote_rc3) and
[http://s.apache.org/kylin-0.7.1-result_rc3](http://s.apache.org/kylin-0.7.1-result_rc3).

{% highlight text %}
To: general@incubator.apache.org
Subject: [VOTE] Release Apache Kylin X.Y.Z (incubating)

Hi all,

The Apache Kylin community has voted on and approved a proposal to release
Apache Kylin X.Y.Z (incubating).

Proposal:
http://s.apache.org/kylin-X.Y.Z-vote_rcN

Vote result:
N binding +1 votes
N non-binding +1 votes
No -1 votes
http://s.apache.org/kylin-X.Y.Z-result_rcN


The commit to be voted upon:
https://github.com/apache/incubator-kylin/commit/XXX

Its hash is XXX.

The artifacts to be voted on are located here:
https://dist.apache.org/repos/dist/dev/incubator/kylin/apache-kylin-X.Y.Z-incubating-rcN/

The hashes of the artifacts are as follows:
src.zip.md5 XXX
src.zip.sha1 XXX
src.tar.gz.md5 XXX
src.tar.gz.sha1 XXX

A staged Maven repository is available for review at:
https://repository.apache.org/content/repositories/orgapachekylin-NNNN/

Release artifacts are signed with the following key:
https://people.apache.org/keys/committer/lukehan.asc

Pursuant to the Releases section of the Incubation Policy and with
the endorsement of our mentors we would now like to request
the permission of the Incubator PMC to publish the release. The vote
is open for 72 hours, or until the necessary number of votes (3 +1)
is reached.

[ ] +1 Release this package
[ ]  0 I don't feel strongly about it, but I'm okay with the release
[ ] -1 Do not release this package because...


Luke Han, on behalf of Apache Kylin PPMC
{% endhighlight %}

After vote finishes, send out the result:
{% highlight text %}
To: general@incubator.apache.org
Subject: [RESULT] [VOTE] Release Apache Kylin X.Y.Z (incubating)

This vote passes with N +1s and no 0 or -1 votes:

+1 <name> (binding or no-binding)

Thanks everyone. We’ll now roll the release out to the mirrors.

Luke Han, on behalf of Apache Kylin PPMC
{% endhighlight %}

## Publishing a release  
After a successful release vote, we need to push the release
out to mirrors, and other tasks.

In JIRA, search for
[all issues resolved in this release](https://issues.apache.org/jira/issues/?jql=project%20%3D%20KYLIN%20),
and do a bulk update changing their status to "Closed",
with a change comment
"Resolved in release X.Y.Z-incubating (YYYY-MM-DD)"
(fill in release number and date appropriately).  
__Uncheck "Send mail for this update".__

Promote the staged nexus artifacts.

* Go to [https://repository.apache.org/](https://repository.apache.org/) and login
* Under "Build Promotion" click "Staging Repositories"
* In the line with "orgapachekylin-xxxx", check the box
* Press "Release" button

Check the artifacts into svn.
{% highlight bash %}
# Get the release candidate.
$ mkdir -p ~/dist/dev
$ cd ~/dist/dev
$ svn co https://dist.apache.org/repos/dist/dev/incubator/kylin

# Copy the artifacts. Note that the copy does not have '-rcN' suffix.
$ mkdir -p ~/dist/release
$ cd ~/dist/release
$ svn co https://dist.apache.org/repos/dist/release/incubator/kylin
$ cd kylin
$ cp -rp ../../dev/kylin/apache-kylin-X.Y.Z-incubating-rcN apache-kylin-X.Y.Z-incubating
$ svn add apache-kylin-X.Y.Z-incubating

# Check in.
svn commit -m 'checkin release artifacts'
{% endhighlight %}

Svnpubsub will publish to
[https://dist.apache.org/repos/dist/release/incubator/kylin](https://dist.apache.org/repos/dist/release/incubator/kylin) and propagate to
[http://www.apache.org/dyn/closer.cgi/incubator/kylin](http://www.apache.org/dyn/closer.cgi/incubator/kylin) within 24 hours.

If there are now more than 2 releases, clear out the oldest ones:

{% highlight bash %}
cd ~/dist/release/kylin
svn rm apache-kylin-X.Y.Z-incubating
svn commit -m 'Remove old release'
{% endhighlight %}

The old releases will remain available in the
[release archive](http://archive.apache.org/dist/incubator/kylin/).

Release same version in JIRA, check [Change Log](https://issues.apache.org/jira/browse/KYLIN/?selectedTab=com.atlassian.jira.jira-projects-plugin:changelog-panel) for the latest released version.

## Publishing the web site  
Refer to [How to document](howto_docs.html) for more detail.

# Thanks  
This guide drafted with reference from [Apache Calcite](http://calcite.incubator.apache.org) Howto doc, Thank you very much.

