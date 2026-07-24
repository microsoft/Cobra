#!perl

#
# guidelines - a Perl module to provide an interface to build-related
# string builders, path builders and other guideline-driven
# Release Engineering processes.
#

# Copyright (c) Nuance Communications, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


use strict;


### ---- Package Definitions ----- ###
package guidelines;
require Exporter;
our @ISA = qw(Exporter);
our @EXPORT = qw
(
    GenBuildname
    GenShortdate
    GenLongdate
    GetBuildname
    GetShortdate
    GetLongdate
);
our $VERSION = '0.01';


### ----- Required Libraries ----- ###
use POSIX qw(strftime);                     # time extraction/parsing


### ----- Required Global Definitions ----- ###
my $g_szBuildname;
my $g_szShortdate;
my $g_szLongdate;


### ----- Forward Prototypes ----- ###
sub GenBuildname($$$$);
sub GenLongdate();
sub GenShortname();
sub GetBuildname();
sub GetLongdate();
sub GetShortname();


### ----- Startup routine ----- ###
# none


### ----- Subroutine Implementations ----- ###

# ---------------------------------------------------------------------------
# Routine:      GenBuildname
# Description:  Will construct and return a name
#               representing this build, given the provided inputs.
# Requires:     None.
# Arguments:    ARG1 is the 'name' of the product
#               ARG2 (optional) is the 'version', 'stage', or 'phase'.
#               -> if not supplied, the version will be omitted
#               ARG3 (optional) is the 'milestone' to use
#               ARG4 (optional) is the timestamp to utilize
#               -> if not supplied, the timestamp will be determined
# Returns:      STRING containing the generated build name
#               Convention proposal/revision as of December 2009
# ---------------------------------------------------------------------------
sub GenBuildname($$$$)
{
    # required declarations
    my $szCodename = shift;
    my $szVersion = shift;
    my $szMilestone = shift;
    my $szTime = shift;
    my $szBuildname = "";

    # construct our buildname
    $szVersion = "" if (not defined($szVersion));
    $szMilestone = "" if (not defined($szMilestone));
    $szTime = "" if (not defined($szTime));
    if (defined($szCodename))
    {
        $szBuildname = "${szCodename}-" if ($szCodename ne "");
        $szBuildname .= "${szVersion}-" if ($szVersion ne "");
        $szBuildname .= "${szMilestone}-" if ($szMilestone ne "");
        if ($szTime eq "")
        {
            # use our pre-determined longdate if available
            GenLongdate() if (not defined($g_szLongdate));
            $szTime = GetLongdate();
        }
        $szBuildname .= $szTime;
    }
    else
    {
        printf("Warning: Argument missing for GenBuildname.\n") if ($^W);
        $szBuildname = "";
    }

    # Set our buildname for later recollection with GetBuildname
    $g_szBuildname = $szBuildname;

    # return our buildname
    return($szBuildname);
}


# ---------------------------------------------------------------------------
# Routine:      GetBuildname
# Description:  Returns the name representing a build.
# Requires:     GetBuildname must run after GenBuildname().
# Arguments:    None.
# Returns:      STRING containing the previously-generated build name.
# ---------------------------------------------------------------------------
sub GetBuildname()
{
    printf("Warning: GetBuildname called before GenBuildname.\n")
        if ($^W and $g_szBuildname eq "");

    # return our buildname
    return($g_szBuildname);
}


# ---------------------------------------------------------------------------
# Routine:      GenShortdate
# Description:  Will construct and return a string representing the least
#               significant date and the number of days that passed in the
#               current year.
# Requires:     None.
# Arguments:    None.
# Returns:      STRING containing the generated short date.
#               Convention proposal/revision as of December 2009
# ---------------------------------------------------------------------------
sub GenShortdate()
{
    # required declarations
    my $szShortdate = strftime("%y%j",localtime);

    # At the time of writing (Dec 31, 2009) there is a leading zero...

    # Set shortdate for later recollection with GetShortdate
    $g_szShortdate = $szShortdate;

    # return our constructed shortdate
    return($szShortdate);
}


# ---------------------------------------------------------------------------
# Routine:      GetShortdate
# Description:  Returns the string representing the short date.
# Requires:     GetShortdate must run after GenShortdate().
# Arguments:    None.
# Returns:      STRING containing the previously-generated short date.
# ---------------------------------------------------------------------------
sub GetShortdate()
{
    printf("Warning: GetShortdate() called before GenShortdate().\n")
        if ($^W and $g_szShortdate eq "");

    # return our short date
    return($g_szShortdate);
}


# ---------------------------------------------------------------------------
# Routine:      GenLongdate
# Description:  Will construct and return a string representing the long
#               form of the date numerically: YYYYMMDDHH.
# Requires:     None.
# Arguments:    None.
# Returns:      STRING containing the generated long date.
#               Convention proposal/revision as of April 2010
# ---------------------------------------------------------------------------
sub GenLongdate()
{
    # required declarations
    my $szLongdate = strftime("%Y%m%d%H",localtime);

    # set longdate for later recollection with GetLongdate
    $g_szLongdate = $szLongdate;

    # return our constructed longdate
    return($g_szLongdate);
}


# ---------------------------------------------------------------------------
# Routine:      GetLongdate
# Description:  Returns the string representing the long date.
# Requires:     GetLongdate must run after GenLongdate().
# Arguments:    None.
# Returns:      STRING containing the previously-generated long date.
# ---------------------------------------------------------------------------
sub GetLongdate()
{
    printf("Warning: GetLongdate() called before GenLongdate().\n")
        if ($^W and $g_szLongdate eq "");

    # return our long date
    return($g_szLongdate);
}


# end
1;
