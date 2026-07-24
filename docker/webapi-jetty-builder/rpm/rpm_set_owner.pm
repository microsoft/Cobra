#
# A simple Linux RPM interface for Perl.
#
package rpm;
require Exporter;
use strict;


### --- Required Package Declarations --- ###
our @ISA = qw(Exporter);
our @EXPORT = ();
our $VERSION = "0.1.0";


### --- Required Libraries --- ###
use Cwd;
use File::Find;
use File::Path;


### --- Global Declarations --- ###
my $DEF_OWNER       = "root";
my $DEF_LOG         = "";
my $DEF_LOGLEVEL    = "1";
my $DEF_TOPDIR      = "/tmp/topdir";
my $DEF_FILES       = "/tmp/files";
my $DEF_NAME        = "Nuance-unnamedrpm";
my $DEF_VER         = "1.0.0";
my $DEF_REL         = "0";
my $DEF_PREFIX      = "/usr/local";
my $DEF_COMPANY     = "Nuance";
my $DEF_VENDOR      = "Nuance Communications, Inc.";
my $DEF_URL         = "http://www.nuance.com";
my $DEF_LICENSE     = "Apache-2.0";
my $DEF_GROUP       = "Applications/Nuance";
my @g_szFilesSection = ();  # used to generate permissions using files on disk


### --- Required Method Forward-Prototypes --- ###
# None


### --- Method Definitions --- ###
# --- Constructor rpm::new() --- #
sub new
{
    # required declarations
    my ($class) = @_;
    my @_RpmGenSpecFindFilesArr = ();
    my $self =
    {
        _arch                   => "",
        _scriptsource           => "",
        _files                  => "",
        _prefix                 => "",
        _postprefix             => "",
        _company                => "",
        _vendor                 => "",
        _url                    => "",
        _license                => "",
        _group                  => "",
        _summary                => "",
        _topdir                 => "",
        _formattedname          => "",
        _longname               => "",
        _name                   => "",
        _ver                    => "",
        _rel                    => "",
        _requires               => "",
        _conflicts              => "",
        _provides               => "",
        _desc                   => "",
        _descfile               => "",
        _autoreqprov            => "",
        _customdeps             => "",
        _scriptlets             => "",
        _triggers               => "",
        _RpmGenSpecFindFiles    => \@_RpmGenSpecFindFilesArr,
        _rpmpathname            => "",
        _log                    => "",
        _loglevel               => "",
        _owner			=> ""
    };
 
    # make this class inheritable
    bless($self,$class);

    # initialize this instance
    $self->__initialize();

    # return an instance of ourselves
    return ($self);
}


# --- "Private Methods" Follow --- #
# --- VOID rpm::__initialize() --- #
sub __initialize()
{
    # required declarations
    my ($self) = @_;
    my $szDefaultArch = "";

    # get our OS arch and use that as our default
    $szDefaultArch = `uname -i`;
    chomp($szDefaultArch);

    # setup our default values
    $self->arch($szDefaultArch);
    $self->topdir($DEF_TOPDIR);
    $self->name($DEF_NAME);
    $self->ver($DEF_VER);
    $self->rel($DEF_REL);
    $self->prefix($DEF_PREFIX);
    $self->company($DEF_COMPANY);
    $self->vendor($DEF_VENDOR);
    $self->url($DEF_URL);
    $self->license($DEF_LICENSE);
    $self->group($DEF_GROUP);
    $self->files($DEF_FILES);
    $self->logfile($DEF_LOG);
    $self->loglevel($DEF_LOGLEVEL);
    $self->owner($DEF_OWNER);

    # the default $self->desc() is determined by $self->MakeSpec()

    # hardwired success result returned
    return(0);
}


# --- STRING rpm::__rpmpathname(optional STRING)
sub __rpmpathname
{
    # required declarations
    my ($self, $szPathname) = @_;

    # set our pathname to 'arg1' is we have it
    if (defined($szPathname))
    {
        $self->{_rpmpathname} = $szPathname;
    }

    # return the current _rpmpathname
    return($self->{_rpmpathname});
}


# --- VOID rpm::__RpmGenSpecFindHandler() --- #
sub __RpmGenSpecFindHandler
{
    # required declarations
    # none

    # add the path to our disgustingly global array
    # disgusting only because we cannot pass our object reference
    push(@g_szFilesSection,$File::Find::name) if ($File::Find::name !~ m/BUILDROOT$/);

    # no return value is processed when invoked through File::Find()
    # it's the thought that counts, though...
}


# --- INT rpm::__logWrite(STRING) --- #
sub __logWrite
{
    # required declarations
    my ($self, $sText) = @_;

    # make sure that we are at least loglevel 1 before displaying
    if ($self->loglevel() > 0)
    {
        if (defined($sText))
        {
            # write/append our text to our log file
            if ($self->logfile() eq "")
            {
                # we don't have anything to open - so use STDERR
                print STDERR $sText;
            }
            else
            {
                # open our log (append)
                if (open(LOG, ">>",$self->{_log}))
                {
                    print LOG $sText;
                    close(LOG);
                }
                else
                {
                    # fallback to STDERR if we can't push the message anywhere else
                    print STDERR "rpm.pm: Can't write/append to log: ${!}!\n" if ("${^WARNING_BITS}");
                }                
            }
        }
        else
        {
            # I'm not worried about this recursion...
            $self->__logWrite("rpm.pm: \$self->__logWrite() needs an argument!\n");
        }
    }
}


# --- INT rpm::__system(STRING) --- #
#
# Wrap Perl's system() - except respect our $self->logfile() and $self->loglevel().
#
sub __system
{
    # required declarations
    my ($self, $szCmdToRun) = @_;
    my $szRedir;
    my $szCmd;
    my $nRC = 0;

    # we only want output if our loglevel > 1
    if ($self->loglevel > 1)
    {
        # we need to output to stdout if no log is defined
        if ($self->logfile() eq "")
        {
            # no log is defined, so we will dump output to stdout
            $szRedir = "";
        }
        else
        {
            # we can now redirect to our log since we have a log 
            $szRedir = " >> " . $self->logfile() . " 2>&1";
        }
    }
    else
    {
        # it turns out that we are not at the proper loglevel to provide output
        # just dump all output to /dev/null in this case
        $szRedir = " >> /dev/null 2>&1";
    }

    # construct szCmd by appending $szRedir to $szCmdToRun
    $szCmd = "${szCmdToRun}${szRedir}";

    # call Perl's system()
    $self->__logWrite("rpm.pm: Calling system($szCmd) ...\n");
    $nRC = system($szCmd);
    $self->__logWrite("rpm.pm: Finished system($szCmd); got $nRC result\n");

    # return our result code
    return($nRC);
}


# --- INT CleanTopDir(VOID) --- #
#
# Will return 0 on success, non-zero on failure
#
sub CleanTopDir
{
    # required declarations
    my ($self) = @_;
    my $szTopdir = $self->topdir();
    my $nRC = 0;

    # make sure we are ready to run CleanTopDir
    if ($szTopdir ne "")
    {
        # completely wipe topdir
        rmtree($szTopdir);
    }
    else
    {
        $nRC = 1;
        $self->__logWrite
        (
            sprintf
            (
                "rpm.pm: Not ready to run CleanTopDir:\n".
                "  topdir is %s\n",
                $szTopdir
            )
        );
    }

    # return success/fail
    return($nRC);
}


# --- INT MakeTopDir(VOID) --- #
#
# Will return 0 on success, non-zero on failure
# Will only create TopDir/* if all subdirectories exist;
#
sub MakeTopDir
{
    # required declarations
    my ($self) = @_;
    my $szPrefix = $self->prefix();
    my $szPostprefix = "";
    my $szScriptsource = "";
    my $szTopdir = $self->topdir();
    my $szTarget = "";
    my $szFiles = $self->files();
    my @szSubdirs =
    (
        "BUILD",
        "BUILDROOT",
        "RPMS",
        "SOURCES",
        "SPECS",
        "SRPMS"
    );
    my $nRC = 0;

    # make sure that we are ready to run GenTopDir
    if ($szTopdir eq "")
    {
        $nRC = 1;
        $self->__logWrite
        (
            sprintf
            (
                "rpm.pm: Not ready to MakeTopDir:\n".
                "  topdir is \"%s\"\n".
                "  scriptsource (optional) is \"%s\"\n",
                $szTopdir,
                $szScriptsource
            )
        );
    }
    else
    {
        # use scriptsource if we have it
        $szScriptsource = $self->scriptsource() if ($self->scriptsource() ne "");

        # create our TOPDIR tree
        foreach (@szSubdirs)
        {
            mkpath("${szTopdir}/${_}");
        }

        # apply a 'post-prefix' (a suffix to the prefix ;)) if we have one
        $szPostprefix = $self->postprefix() if ($self->postprefix() ne "");

        # create the build root tree
        mkpath("${szTopdir}/BUILDROOT${szPrefix}${szPostprefix}");
        $szTarget = "${szTopdir}/BUILDROOT${szPrefix}${szPostprefix}";
        $nRC = system("cp -r ${szFiles}/* ${szTarget}");

        # make sure any custom sources are rolled in
        if (defined($szScriptsource) && $szScriptsource ne "")
        {
            if ( -d "${szScriptsource}/SOURCES" )
            {
                $self->__system("cp -f ${szScriptsource}/SOURCES/* ${szTopdir}/SOURCES");
            }
            else
            {
                $self->__system("touch ${szTopdir}/SOURCES/linux-find-requires.sh");
                $self->__system("touch ${szTopdir}/SOURCES/linux-find-provides.sh");
            }

            # make sure that any scripts in SOURCES are executable
            $self->__system("chmod +x ${szTopdir}/SOURCES/*.sh");
            $self->__system("chmod +x ${szTopdir}/SOURCES/*.csh");
            $self->__system("chmod +x ${szTopdir}/SOURCES/*.bash");
        }
        else
        {
            $self->__logWrite
            (
                sprintf
                (
                    "rpm.pm: Skipping SOURCES since scriptsource is not defined for topdir %s.\n",
                    $szTopdir
                )
            );
        }
    }

    # return success/fail
    return($nRC);
}


# --- INT MakeSpec(VOID) --- #
#
# Will return 0 on success, non-zero on failure
#
sub MakeSpec
{
    # required declarations
    my ($self) = @_;
    my $szTopdir = $self->topdir();
    my $szBuildroot = "${szTopdir}/BUILDROOT";
    my $szFormattedname = $self->formattedname();
    my $szName = $self->name();
    my $szVer = $self->ver();
    my $szRel = $self->rel();
    my $szLicense = $self->license();
    my $szGroup = $self->group();
    my $szSummary = $self->summary();
    my $szVendor = $self->vendor();
    my $szUrl = $self->url();
    my $szPrefix = $self->prefix();
    my $szRequires = $self->requires();
    my $szConflicts = $self->conflicts();
    my $szProvides = $self->provides();
    my $szDesc = $self->desc();
    my $szDescfile = $self->descfile();
    my @szDesccontents = ();
    my $szSpecname = "${szName}-${szVer}-${szRel}";
    my @szSpeccontents = ();
    my $szAutoReqProv = $self->autoreqprov();
    my $szCustomdeps = $self->customdeps();
    my @szScriptletcontents = ();
    my $szScriptlets = $self->scriptlets();
    my @szTriggercontents = ();
    my $szTriggers = $self->triggers();
    my $szPostprefix = $self->postprefix();
    my $szFilestring = "";
    my $szSpecfilename = "${szTopdir}/SPECS/${szSpecname}.spec";
    my $szFiles = $self->files();
    my $szCpTarget = "${szTopdir}/BUILDROOT${szPrefix}${szPostprefix}";
    my $szOwner = $self->owner();
    my $nRC = 0;

    # formattedname may use name if it isn't available
    $szFormattedname = $self->name() if ($szFormattedname eq "");

    # summary may use formattedname if it isn't available
    $szSummary = $szFormattedname if ($szSummary eq "");

    # make sure that we are ready to run MakeSpec
    if ($szFiles eq "" || $szTopdir eq "" || $szFormattedname eq "" ||
        $szName eq "" || $szVer eq "" || $szRel eq "" || $szLicense eq "" ||
        $szGroup eq "" || $szVendor eq "" || $szUrl eq "" || $szPrefix eq "")
    {
        $nRC = 1;
        $self->__logWrite
        (
            sprintf
            (
                "rpm.pm: Not ready to MakeSpec:\n".
                "  files is \"%s\"\n".
                "  topdir is \"%s\"\n".
                "  formattedname is \"%s\"\n".
                "  name is \"%s\"\n".
                "  ver is \"%s\"\n".
                "  rel is \"%s\"\n".
                "  license is \"%s\"\n".
                "  group is \"%s\"\n".
                "  summary is \"%s\"\n".
                "  vendor is \"%s\"\n".
                "  url is \"%s\"\n".
                "  prefix is \"%s\"\n".
                "  requires (optional) is \"%s\"\n".
                "  conflicts (optional) is \"%s\"\n".
                "  provides (optional) is \"%s\"\n".
                "  desc (optional) is \"%s\"\n".
                "  customdeps (optional) is \"%s\"\n".
                "  scriptlets (optional) is \"%s\"\n".
                "  triggers (optional) is \"%s\"\n",
                "  postprefix (optional) is \"%s\"\n",
                $szFiles,
                $szTopdir,
                $szFormattedname,
                $szName,
                $szVer,
                $szRel,
                $szLicense,
                $szGroup,
                $szSummary,
                $szVendor,
                $szUrl,
                $szPrefix,
                $szRequires,
                $szConflicts,
                $szProvides,
                $szDesc,
                $szCustomdeps,
                $szScriptlets,
                $szTriggers,
                $szPostprefix,
                $szOwner
            )
        );
    }
    else
    {
        # the RPM jar repackaging ruins the integrity of our own product JARs
        # we suppress it here since it provides no value even if it 'worked'
        push(@szSpeccontents,"\%define __os_install_post %{nil}\n");

        # we use our own custom topdir to override the /usr/src/redhat topdir
        push(@szSpeccontents,"\%define _topdir ${szTopdir}\n");

        push(@szSpeccontents,"\%define _binaries_in_noarch_packages_terminate_build 0\n");

        # if we are to completely suppress automatic requires/provides detection
        # we assume that it is enabled
        if ($szAutoReqProv =~ m/^[Nn]/ || $szAutoReqProv =~ m/^0/)
        {
            push(@szSpeccontents,"AutoReqProv: no\n");
        }

        # if we have customdeps, then find-provides and find-requires is supplied
        if ($szCustomdeps ne "")
        {
            push
            (
                @szSpeccontents,
                "Source91: linux-find-requires.sh\n",
                "Source92: linux-find-provides.sh\n",
                "\%define _use_internal_dependency_generator 0\n",
                "\%define __find_requires %{SOURCE91}\n",
                "\%define __find_provides %{SOURCE92}\n"
            );
        }

        # standard SPEC information
        push(@szSpeccontents,
            "BuildRoot: ${szBuildroot}\n",
            "Summary: ${szSummary}\n",
            "Name: ${szName}\n",
            "Version: ${szVer}\n",
            "Release: ${szRel}\n",
            "License: ${szLicense}\n",
            "Group: ${szGroup}\n",
            "Vendor: ${szVendor}\n",
            "URL: ${szUrl}\n",
            "Prefix: ${szPrefix}\n"
        );

        # Add optional tags as defined by values in object
        push(@szSpeccontents,"Requires: ${szRequires}\n")
            if ($szRequires ne "");
        push(@szSpeccontents,"Conflicts: ${szConflicts}\n")
            if ($szConflicts ne "");
        push(@szSpeccontents,"Provides: ${szProvides}\n")
            if ($szProvides ne "");

        # If a description was supplied add it
        push(@szSpeccontents,"\%description\n");
        if (defined($szDesc) && $szDesc ne "")
        {
            push(@szSpeccontents,$szDesc);
        }
        else
        {
            # try a description file
            if (open(DESC,$szDescfile))
            {
                @szDesccontents = <DESC>;
                close(DESC);
                push(@szSpeccontents,@szDesccontents);
            }
            else
            {
                # use a default message if both a desc or descfile isn't available
                push
                (
                    @szSpeccontents,
                    "This RPM contains ${szFormattedname} from ${szVendor}.\n"
                );
            }
        }

        # create a meaningful %install section due to the RPM 4.8.x behaviour
        # of casually removing everything that's in there before rpmbuild runs
        push
        (
            @szSpeccontents,
            "\n",
            "%install\n",
            "/bin/mkdir -p \"${szCpTarget}\"\n",
            "/bin/cp -r ${szFiles}/* ${szCpTarget}\n"
        );

        # we currently do not care about any of these sections since we always
        # build from a predetermined set of binaries
        push(@szSpeccontents,
            "\n",
            "#\%prep\n",
            "#\%setup -q\n",
            "#\%build\n",
            "#\%clean\n",
            "\n",
            "\%files\n"
        );

        # generate the %files section (using our global array/hack)
        # make sure that this array is clean
        @g_szFilesSection = ();

        # populate the global array with the contents of szBuildroot
        find(\&__RpmGenSpecFindHandler,$szBuildroot);

        # take a shower for using dirty solution
        @{$self->{_RpmGenSpecFindFiles}} = @g_szFilesSection;

        # build the remaining spec file entries for the %files section
        my $teststring = "${szTopdir}/BUILDROOT${szPrefix}${szPostprefix}";
        $teststring =~ s/\/$//;
        foreach (@{$self->{_RpmGenSpecFindFiles}})
        {
            # if the item is a directory
            if ( -d && ! -l)
            {
                if ( "${_}" lt "$teststring" ) {
                    ## print "DEBUG: ${_} is lt $teststring\n";
                    $szFilestring = "\%attr(-, -, -) \%dir \"${_}\"";
                } 
                else 
                {
                    ## print "DEBUG: ${_} is ge $teststring\n";
                    $szFilestring = "\%attr(755, $szOwner, $szOwner) \%dir \"${_}\"";
                }
            }
            else
            {
                if ( m|/bin/| or m|/lib/| or m|\.so$| or m|\.sh$| or m|\.csh$| )
                {
                    # the item is meant to be executable
                    $szFilestring = "\%attr(755, $szOwner, $szOwner) \"${_}\"";
                }
                elsif ( m|/config/| or m|/etc/| )
                {
                    # the item is meant to be a configuration file
                    $szFilestring = "\%attr(644, $szOwner, $szOwner) \%config \"${_}\"";
                }
                elsif ( m|/doc/| or m|/nuan_doc/| or m|documentation/| or m|Documentation/| )
                {
                    # the item is meant to be a documentation file
                    $szFilestring = "\%attr(644, $szOwner, $szOwner) \%doc \"${_}\"";
                }
                elsif ( -l )
                {
                    $szFilestring = "\%attr(-, $szOwner, $szOwner) \"${_}\"";
                }
                else
                {
                    # catch-all file category
                    $szFilestring = "\%attr(644, $szOwner, $szOwner) \"${_}\"";
                }
            }

            # filter out the buildroot path add the revised entry to our SPEC
            $szFilestring =~ s/$szBuildroot//;
            push(@szSpeccontents,"${szFilestring}\n");
        }

        # source-in any scriptlets that may have been defined and are available
        if (open(SCRIPTLETS,$szScriptlets))
        {
            @szScriptletcontents = <SCRIPTLETS>;
            push(@szSpeccontents,@szScriptletcontents);
            close(SCRIPTLETS);
        }
        if (open(TRIGGERS,$szTriggers))
        {
            @szTriggercontents = <TRIGGERS>;
            push(@szSpeccontents,@szTriggercontents);
            close(TRIGGERS);
        }

        # write out our spec file
        if (open(SPECFILE,"> ${szSpecfilename}"))
        {
            print SPECFILE @szSpeccontents;
            close(SPECFILE);
        }
        else
        {
            $self->__logWrite
            (
                sprintf
                (
                    "rpm.pm: Unable to open SPEC file for writing: %s!  The SPEC would be \"%s\".\n",
                    $szSpecfilename,
                    $!
                )
            );
            $nRC = 1;
        }
    }

    # return success/fail
    return($nRC);
}


# --- INT MakeRPM(VOID) --- #
#
# Will return 0 on success, non-zero on failure
#
sub MakeRPM
{
    # required declarations
    my ($self) = @_;
    my $szWorkingdir = cwd();
    my $szTopdir = $self->topdir();
    my $szName = $self->name();
    my $szSpecfile = "";
    my $szArch = $self->arch();
    my $szRPM = "";
    my $szSH = "";
    my $szCmd = "";
    my $nRC = 0;

    # make sure that we are ready to run MakeRPM()
    if ($szTopdir eq "" || $szName eq "")
    {
        $nRC = 1;
        $self->__logWrite
        (
            sprintf
            (
                "rpm.pm: Not ready to MakeRPM:\n".
                "  topdir is \"%s\"\n".
                "  name is \"%s\"\n".
                $szTopdir,
                $szName
            )
        );
    }
    else
    {
        # try to build our RPM from the SPEC
        chdir($szTopdir);

        # find our specfile
        if (opendir(SPECS,"${szTopdir}/SPECS"))
        {
            ($szSpecfile) = grep {/^$szName(.*)\.spec$/} readdir(SPECS);
            closedir(SPECS);

            # normally target and buildroot are defined in the spec file but
            # rpmbuild has a bug where target and buildroot are both not
            # respected if defined in the spec; so we pass them to rpmbuild
            $szCmd = "rpmbuild --target ${szArch} -bb SPECS/${szSpecfile} --buildroot=${szTopdir}/BUILDROOT";
            $nRC = $self->__system($szCmd);
            if ($nRC)
            {
                $self->__logWrite(sprintf("rpm.pm: Unable to build RPM.  Return code was %s.\n",$nRC));
            }
            else
            {
                # set our _rpmpathname string
                if (opendir(RPMS,"${szTopdir}/RPMS/${szArch}"))
                {
                    # lookup the RPM filename generated
                     ($szRPM) = grep {/^$szName(.*)\.rpm$/} readdir(RPMS);
                    closedir(RPMS);

                    # provided the filename lookup worked, set _rpmpathname
                    if (defined($szRPM))
                    {
                        $self->__rpmpathname("${szTopdir}/RPMS/${szArch}/${szRPM}");
                    }
                }
                else
                {
                    $self->__logWrite(sprintf("rpm.pm: Unable to set _rpmpathname!\n"));
                }
            }
        }
        else
        {
            $nRC = 1;
            $self->__logWrite
            (
                sprintf
                (
                    "rpm.pm: Unable to determine SPEC from name!\n  name=%s\n  topdir=%s.\n",
                    $szName,
                    $szTopdir
                )
            );
        }

        chdir($szWorkingdir);
    }

    # return success/fail
    return($nRC);
}


# --- STRING GetRPMPathname(VOID) --- #
#
# Will return the pathname to the built RPM file.
#
sub GetRPMPathname
{
    # required declarations
    my ($self) = @_;

    # return the value of _rpmpathname as-is
    return($self->__rpmpathname());
}



# --- STRING Build(VOID) --- #
#
# Invoke ALL steps, assuming all required values are set.
# Build will invoke CleanTopDir, MakeTopDir, MakeSpec, MakeRPM and GetRPMName.
# Build() will return the RPM pathname returned by GetRPMName.
#
sub Build
{
    # required declarations
    my ($self) = @_;
    my $szRPMPathname = "";
    my $nRC = 0;

    # run CleanTopDir
    $nRC = $self->CleanTopDir();
    if ($nRC)
    {
        $self->__logWrite(sprintf("rpm.pm: Build aborting due to previous error(s) in CleanTopDir().\n"));
    }
    else
    {
        # run MakeTopDir
        $nRC = $self->MakeTopDir();
        if ($nRC)
        {
            $self->__logWrite(sprintf("rpm.pm: Build aborting due to previous error(s) in MakeTopDir().\n"));
        }
        else
        {
            # run MakeSpec
            $nRC = $self->MakeSpec();
            if ($nRC)
            {
                $self->__logWrite(sprintf("rpm.pm: Build aborting due to previous error(s) in MakeSpec().\n"));
            }
            else
            {
                # run MakeRPM
                $nRC = $self->MakeRPM();
                if ($nRC)
                {
                    $self->__logWrite(sprintf("rpm.pm: Build aborting due to previous error(s) in MakeRPM().\n"));
                }
                else
                {
                    # fetch our RPM pathname
                    $szRPMPathname = $self->GetRPMPathname();
                }
            }
        }
    }

    # return our RPM pathname
    return($szRPMPathname);
}


# --- Getter/Setter Methods Follow --- #
# --- STRING rpm::arch(optional STRING) --- #
sub arch
{
    # required declarations
    my ($self,$szArch) = @_;

    if (defined($szArch))
    {
        # set the arch if specified
        $self->{_arch} = $szArch;
    }

    # return our arch
    return($self->{_arch});
}


# --- STRING rpm::scriptource(optional STRING) --- #
sub scriptsource
{
    # required declarations
    my ($self,$szScriptsource) = @_;

    if (defined($szScriptsource))
    {
        # set scriptsource if specified
        $self->{_scriptsource} = $szScriptsource;
    }

    # return our scriptsource
    return($self->{_scriptsource});
}


# --- STRING rpm::files(optional STRING) --- #
sub files
{
    # required declarations
    my ($self,$szFiles) = @_;

    if (defined($szFiles))
    {
        # set files if specified
        $self->{_files} = $szFiles;
    }

    # return our files
    return($self->{_files});
}


# --- STRING rpm::prefix(optional STRING) --- #
sub prefix
{
    # required declarations
    my ($self,$szPrefix) = @_;

    if (defined($szPrefix))
    {
        if ($szPrefix =~ m|^/| )
        {
            # set prefix if specified
            $self->{_prefix} = $szPrefix;
        }
        else
        {
            $self->__logWrite("rpm.pm: postprefix() value needs to start with '/'!\n");
        }
    }

    # return our prefix
    return($self->{_prefix});
}


# --- STRING rpm::postprefix(optional STRING) --- #
sub postprefix
{
    # required declarations
    my ($self,$szPostprefix) = @_;

    if (defined($szPostprefix))
    {
        if ($szPostprefix =~ m|^/| )
        {
            # set postprefix if specified
            $self->{_postprefix} = $szPostprefix;
        }
        else
        {
            $self->__logWrite("rpm.pm: postprefix() value needs to start with '/'!\n");
        }
    }

    # return our postprefix
    return($self->{_postprefix});
}


# --- STRING rpm::company(optional STRING) --- #
sub company
{
    # required declarations
    my ($self,$szCompany) = @_;

    if (defined($szCompany))
    {
        # set company if specified
        $self->{_company} = $szCompany;
    }

    # return our company
    return($self->{_company});
}


# --- STRING rpm::vendor(optional STRING) --- #
sub vendor
{
    # required declarations
    my ($self,$szVendor) = @_;

    if (defined($szVendor))
    {
        # set vendor if specified
        $self->{_vendor} = $szVendor;
    }

    # return our vendor
    return($self->{_vendor});
}


# --- STRING rpm::url(optional STRING) --- #
sub url
{
    # required declarations
    my ($self,$szUrl) = @_;

    if (defined($szUrl))
    {
        # set url if specified
        $self->{_url} = $szUrl;
    }

    # return our url
    return($self->{_url});
}


# --- STRING rpm::license(optional STRING) --- #
sub license
{
    # required declarations
    my ($self,$szLicense) = @_;

    if (defined($szLicense))
    {
        # set license if specified
        $self->{_license} = $szLicense;
    }

    # return our license
    return($self->{_license});
}


# --- STRING rpm::group(optional STRING) --- #
sub group
{
    # required declarations
    my ($self,$szGroup) = @_;

    if (defined($szGroup))
    {
        # set group if specified
        $self->{_group} = $szGroup;
    }

    # return our group
    return($self->{_group});
}


# --- STRING rpm::summary(optional STRING) --- #
sub summary
{
    # required declarations
    my ($self,$szSummary) = @_;

    if (defined($szSummary))
    {
        # set summary if specified
        $self->{_summary} = $szSummary;
    }

    # return our summary
    return($self->{_summary});
}


# --- STRING rpm::topdir(optional STRING) --- #
sub topdir
{
    # required declarations
    my ($self,$szTopdir) = @_;

    if (defined($szTopdir))
    {
        # set topdir if specified
        $self->{_topdir} = $szTopdir;
    }

    # return our topdir
    return($self->{_topdir});
}


# --- STRING rpm::longname(optional STRING) --- #
sub longname
{
    # required declarations
    my ($self,$szLongname) = @_;

    if (defined($szLongname))
    {
        # set longname if specified
        $self->{_longname} = $szLongname;
    }

    # return our longname
    return($self->{_longname});
}


# --- STRING rpm::formattedname(optional STRING) --- #
sub formattedname
{
    # required declarations
    my ($self,$szFormattedname) = @_;

    if (defined($szFormattedname))
    {
        # set formattedname if specified
        $self->{_formattedname} = $szFormattedname;
    }

    # return our formattedname
    return($self->{_formattedname});
}


# --- STRING rpm::name(optional STRING) --- #
sub name
{
    # required declarations
    my ($self,$szName) = @_;

    if (defined($szName))
    {
        # set name if specified
        $self->{_name} = $szName;
    }

    # return our name
    return($self->{_name});
}


# --- STRING rpm::ver(optional STRING) --- #
sub ver
{
    # required declarations
    my ($self,$szVer) = @_;

    if (defined($szVer))
    {
        # set ver if specified
        $self->{_ver} = $szVer;
    }

    # return our name
    return($self->{_ver});
}


# --- STRING rpm::rel(optional STRING) --- #
sub rel
{
    # required declarations
    my ($self,$szRel) = @_;

    if (defined($szRel))
    {
        # set rel if specified
        $self->{_rel} = $szRel;
    }

    # return our rel
    return($self->{_rel});
}

# --- STRING rpm::owner(optional STRING) --- #
sub owner
{
    # required declarations
    my ($self,$szOwner) = @_;

    if (defined($szOwner))
    {
        # set owner if specified
        $self->{_owner} = $szOwner;
    }

    # return our owner
    return($self->{_owner});
}


# --- STRING rpm::requires(optional STRING) --- #
sub requires
{
    # required declarations
    my ($self,$szRequires) = @_;

    if (defined($szRequires))
    {
        # set requires if specified
        $self->{_requires} = $szRequires;
    }

    # return our requires
    return($self->{_requires});
}


# --- STRING rpm::conflicts(optional STRING) --- #
sub conflicts
{
    # required declarations
    my ($self,$szConflicts) = @_;

    if (defined($szConflicts))
    {
        # set conflicts if specified
        $self->{_conflicts} = $szConflicts;
    }

    # return our conflicts
    return($self->{_conflicts});
}


# --- STRING rpm::provides(optional STRING) --- #
sub provides
{
    # required declarations
    my ($self,$szProvides) = @_;

    if (defined($szProvides))
    {
        # set provides if specified
        $self->{_provides} = $szProvides;
    }

    # return our provides
    return($self->{_provides});
}


# --- STRING rpm::desc(optional STRING) --- #
sub desc
{
    # required declarations
    my ($self,$szDesc) = @_;

    if (defined($szDesc))
    {
        # set desc if specified
        $self->{_desc} = $szDesc;
    }

    # return our description
    return($self->{_desc});
}


# --- STRING Linux::rpm::descfile(optional STRING) --- #
sub descfile
{
    # required declarations
    my ($self,$szDescfile) = @_;

    if (defined($szDescfile))
    {
        # set descfile if specified
        $self->{_descfile} = $szDescfile;
    }

    # return our descfile
    return($self->{_descfile});
}


# --- STRING rpm::autoreqprov(optional STRING) --- #
sub autoreqprov
{
    # required declarations
    my ($self,$szAutoReqProv) = @_;

    if (defined($szAutoReqProv))
    {
        # set autoreqprov if specified
        $self->{_autoreqprov} = $szAutoReqProv;
    }

    # return our autoreqprov value
    return($self->{_autoreqprov});
}


# --- STRING rpm::customdeps(optional STRING) --- #
sub customdeps
{
    # required declarations
    my ($self,$szCustomdeps) = @_;

    if (defined($szCustomdeps))
    {
        # set customdeps if specified
        $self->{_customdeps} = $szCustomdeps;
    }

    # return our customdeps
    return($self->{_customdeps});
}


# --- STRING rpm::scriptlets(optional STRING) --- #
sub scriptlets
{
    # required declarations
    my ($self,$szScriptlets) = @_;

    if (defined($szScriptlets))
    {
        # set scriptlets if specified
        $self->{_scriptlets} = $szScriptlets;
    }

    # return our scriptlets
    return($self->{_scriptlets});
}


# --- STRING rpm::triggers(optional STRING) --- #
sub triggers
{
    # required declarations
    my ($self,$szTriggers) = @_;

    if (defined($szTriggers))
    {
        # set triggers if specified
        $self->{_triggers} = $szTriggers;
    }

    # return our triggers
    return($self->{_triggers});
}


# --- STRING rpm::logfile(optional STRING) --- #
#
# Will return the log file currently in use or define a new log if
# the expected STRING argument is available.
# STDOUT/STDERR will be used if log = "".
#
sub logfile
{
    # required declarations
    my ($self,$szLog) = @_;

    if (defined($szLog))
    {
        # set our new log
        $self->{_log} = $szLog;
    }

    # return the value of _log
    return($self->{_log});
}


# --- INT loglevel(INT LogLevel) --- #
#
# Will return the defined verbosity level, or set it if
# the expected argument is available.
# Where LogLevel:
# 0 is very quiet
# 1 shows rpm.pm messages
# 2 shows all command output and exit status
#
sub loglevel
{
    # required declarations
    my ($self,$nLogLevel) = @_;

    if (defined($nLogLevel))
    {
        $self->{_loglevel} = $nLogLevel;
    }

    # return our _loglevel
    return($self->{_loglevel});
}


# end
1;
__END__
