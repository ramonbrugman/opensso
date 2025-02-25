#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2007 Sun Microsystems Inc. All Rights Reserved
#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at
# https://opensso.dev.java.net/public/CDDLv1.0.html or
# opensso/legal/CDDLv1.0.txt
# See the License for the specific language governing
# permission and limitations under the License.
#
# When distributing Covered Code, include this CDDL
# Header Notice in each file and include the License file
# at opensso/legal/CDDLv1.0.txt.
# If applicable, add the following below the CDDL Header,
# with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# $Id: amtune-ws7.pl,v 1.4 2008-08-19 19:08:36 veiming Exp $
#
#
#############################################################################

use File::Basename;

$AMTUNE_SCRIPT_RECORD_STRING="amtune-ws7.pl|ws|1";

require "amtune-utils.pl";
require "amtune-env.pl";


my $SCRIPT_LOCATION=dirname($0);



#-------------------------------------------------------------------------------
# Description	:   Delete one or more jvm option(s)
#-------------------------------------------------------------------------------

sub deleteJVMOptionUsingWSAdmin
{
	($jvmOptionString,$flag)=@_;
	
	if(( $jvmOptionString eq "<No value set>" ) || ( $jvmOptionString eq "")  || ( $jvmOptionString eq "<Empty value>"))
	{
		return ;
	}
	
	print "\n Deleting JVM Option : $jvmOptionString\n";
	
	#print $WSADMIN delete-jvm-options $WSADMIN_COMMON_PARAMS $jvmOptionString;
	
	if( $flag eq combined)
	{
		$WSADMIN_OPTION="delete-jvm-options";
		$WSADMIN_OPTION1="--";
		$WSADMIN_OPTION_JVMOPTIONSTRING="$jvmOptionString";
		@args=($WSADMIN,$WSADMIN_OPTION,@WSADMIN_COMMON_PARAMS,$WSADMIN_OPTION1,$WSADMIN_OPTION_JVMOPTIONSTRING);
		system(@args)==0 or die "\n Error executing command @args\n";
		#`$WSADMIN delete-jvm-options $WSADMIN_COMMON_PARAMS -- "$jvmOptionString"`;
	}
	else
	{
		$WSADMIN_OPTION="delete-jvm-options";
		$WSADMIN_OPTION1="--";
		@WSADMIN_OPTION_JVMOPTIONSTRING=split(" ",$jvmOptionString);
		@args=($WSADMIN,$WSADMIN_OPTION,@WSADMIN_COMMON_PARAMS,$WSADMIN_OPTION1,@WSADMIN_OPTION_JVMOPTIONSTRING);
		system(@args)==0 or die "\n Error executing command @args\n";
		#`$WSADMIN delete-jvm-option $WSADMIN_COMMON_PARAMS -- $jvmOptionstring`;
	}
	
	#print $error_message;
}

#-------------------------------------------------------------------------------
# Description	:   Create one or more jvm option(s)
#-------------------------------------------------------------------------------

sub insertJVMOptionUsingWSAdmin
{
	($jvmOptionString,$flag)=@_;

	if($jvmOptionString ne "")
	{
		#print "Inserting JVM Option  :  $jvmOptionString\n";
		
		#print $WSADMIN create-jvm-options $WSADMIN_COMMON_PARAMS $jvmOptionString;
		if($flag eq combined)
		{
			$WSADMIN_OPTION="create-jvm-options";
			$WSADMIN_OPTION1="--";
			$WSADMIN_OPTION_JVMOPTIONSTRING="$jvmOptionString";
			@args=($WSADMIN,$WSADMIN_OPTION,@WSADMIN_COMMON_PARAMS,$WSADMIN_OPTION1,$WSADMIN_OPTION_JVMOPTIONSTRING);
			system(@args)==0 or die "\n Error executing command @args\n";
		}
		else
		{
			$WSADMIN_OPTION="create-jvm-options";
			$WSADMIN_OPTION1="--";
			@WSADMIN_OPTION_JVMOPTIONSTRING=split(" ",$jvmOptionString);
			@args=($WSADMIN,$WSADMIN_OPTION,@WSADMIN_COMMON_PARAMS,$WSADMIN_OPTION1,@WSADMIN_OPTION_JVMOPTIONSTRING);
			system(@args)==0 or die "\n Error executing command @args\n";
		}
	}
	return $?;
}

#-------------------------------------------------------------------------------
# Description	:   This function is used to return a value of parameters in thread-pool
#		    http listener, and jvm
#-------------------------------------------------------------------------------

sub getServerProp
{
	@array=@_;
	for($i=0;$i<($#array);$i++)
	{
		push(@serverPropString,$array[$i]);
	}
	$propName=$array[$#array];
		
	if(($#serverPropString == -1) || ($propName eq ""))
	{
		return;
	}
	
	@data=grep(m/$propName/,@serverPropString);
	@value=split(m/=/,$data[0]);
	return $value[1];
}

#-------------------------------------------------------------------------------
# Description   :   Search and return a specific jvm option in a string that was
#		    generated by web server admin list-jvm-otions command 
#-------------------------------------------------------------------------------
sub getWSJVMOption
{
	($option_key,$option_type,$field_separator,$nvp_separator)=@_;
	
	if($#array == -1)
	{
		return;
	}
	
	$variable=get_token_in_line(@array,$option_key,$option_key,$option_type,"stream",$field_separator,$nvp_separator);
	return $variable;
}

#-------------------------------------------------------------------------------
# Description   :   Concatenate to the main string if input string is not empty,
#                   not equal to <No value set> nor <Empty value> string
#-------------------------------------------------------------------------------
sub createParamString
{
	($paramString,$inputString)=@_;
	chomp($paramString);
	chomp($inputString);
	if(($inputString ne <No value set>) && ($inputString ne "") && ($inputString ne <Empty value>))
	{
		if($paramString eq "")
		{
			$paramString = $inputString;
		}
		else
		{
			
			$paramString = "$paramString"." "."$inputString";
		}
	}
	return $paramString."\n";
}

#-------------------------------------------------------------------------------
# Description   :   Main function that executes web server administration command
#                   "wadm" to modify web server configuration
#-------------------------------------------------------------------------------

sub tuneServerXML
{
	$tune_file="$CONTAINER_INSTANCE_DIR/config/server.xml";
	
	&echo_msg( $LINE_SEP);&echo_msg("\n");
	&echo_msg( " Tuning Web Server Instance...\n");
	&echo_msg( " File			: $tune_file(using wadm command line tool)\n");
	&echo_msg( " Parameter tuning 	: \n");
	
	$temp=$ENV{TEMP};
	$WSADMIN_OPTION_THREAD_POOL="get-thread-pool-prop";
	@args=($WSADMIN,$WSADMIN_OPTION_THREAD_POOL,@WSADMIN_COMMON_PARAMS,">$temp/temp.txt");
	system(@args)==0 or die "\n Error executing command @args\n";
	open(FP,"$temp/temp.txt");
	@threadpool_string=<FP>;
	close(FP);
	
	$wsadmin_min_threads=getServerProp(@threadpool_string,"min-threads");
	
	&echo_msg( " 1.	Minimum Threads\n");
	&echo_msg( " Current Value		: min-threads=$wsadmin_min_threads");
	&echo_msg( " Recommended Value	: min-threads=$AMTUNE_NUM_WS_MIN_THREADS\n\n");
	
	$wsadmin_max_threads=getServerProp(@threadpool_string,"max-threads");
	
	&echo_msg( " 2.	Maximum Threads\n");
	&echo_msg( " Current Value		: max-threads=$wsadmin_max_threads");
	&echo_msg( " Recommended Value	: max-threads=$numOfMaxThreadPool\n\n");
	
	$wsadmin_queue_size=getServerProp(@threadpool_string,"queue-size");
	
	&echo_msg( " 3.	Queue Size\n");
	&echo_msg( " Current Value		: queue-size=$wsadmin_queue_size");
	&echo_msg( " Recommended Value	: queue-size=$AMTUNE_NUM_TCP_CONN_SIZE\n\n");
	
	$wsadmin_native_stack_size=getServerProp(@threadpool_string,"stack-size");
	
	&echo_msg( " 4.	Native Stack Size\n");
	&echo_msg( " Current Value		: stack-size=$wsadmin_native_stack_size");
	if($JVM64bitAvailable eq true)
	{
		&echo_msg( " Recommended Value	: stack-size=$AMTUNE_NATIVE_STACK_SIZE_64_BIT\n\n");
	}
	else
	{
		&echo_msg( " Recommended Value	: Use current value\n\n");
	}
	
	$WSADMIN_OPTION_HTTP_LISTENER="get-http-listener-prop";
	$WSADMIN_OPTION_HTTP="--http-listener=$WSADMIN_HTTPLISTENER";
	@args=($WSADMIN,$WSADMIN_OPTION_HTTP_LISTENER,@WSADMIN_COMMON_PARAMS,$WSADMIN_OPTION_HTTP,">$temp/temp.txt");
	system(@args)==0 or die "\n Error executing command @args\n";
	open(FP,"$temp/temp.txt");
	@httplistener_string=<FP>;
	close(FP);
	
	$wsadmin_acceptor_threads=getServerProp(@httplistener_string,"acceptor-threads");
	
	&echo_msg( " 5.	Acceptor Threads\n");
	&echo_msg( " Current value		: acceptor-threads=$wsadmin_acceptor_threads");
	&echo_msg( " Recommended value 	: acceptor-threads=$acceptorThreads\n\n");
	
	$WSADMIN_OPTION_STATS_PROP="get-stats-prop";
	@args=($WSADMIN,$WSADMIN_OPTION_STATS_PROP,@WSADMIN_COMMON_PARAMS,">$temp/temp.txt");
	system(@args);
	open(FP,"$temp/temp.txt");
	@stat_prop_string=<FP>;
	close(FP);
	$wsadmin_stat_prop=getServerProp(@stat_prop_string,"enabled");
	
	&echo_msg( " 6.   	Statistic\n");
	&echo_msg( " Current Value        : enabled=$wsadmin_stat_prop");
	&echo_msg( " Recommended Value    : enabled=$AMTUNE_STATISTIC_ENABLED\n\n");
       
	
	$WSADMIN_OPTION="list-jvm-options";
	@args=($WSADMIN,$WSADMIN_OPTION,@WSADMIN_COMMON_PARAMS,">$temp/temp.txt");
	system(@args)==0 or die "\n Error executing command @args\n";
	open(FP,"$temp/temp.txt");
	@array=<FP>;
	close(FP);
	
	$wsadmin_min_heap=getWSJVMOption("-Xms","flag"," ");
	$wsadmin_new_min_heap="-Xms"."$minHeapSize"."M";
	
	$wsadmin_max_heap=getWSJVMOption("-Xmx","flag"," ");
	$wsadmin_new_max_heap="-Xmx"."$maxHeapSize"."M";
	
	&echo_msg( " 7. Max and Min Heap Size\n");
	&echo_msg( " Current value		: Min Heap: $wsadmin_min_heap Max Heap:$wsadmin_max_heap");
	if(($JVMbitAvailable eq true) && ( $memToUse gt $amtuneMaxMemoryToUseInMB))
	{
		displayJVM64bitMessage($wsadmin_new_min_heap,$wsadmin_new_max_heap);
	}
	else
	{
		&echo_msg( " Recommended  value	: $wsadmin_new_min_heap $wsadmin_new_max_heap\n\n");
	}
	
	$wsadmin_loggc_output=getWSJVMOption("-Xloggc","flag"," ");
	$wsadmin_new_loggc_output="-Xloggc:$CONTAINER_INSTANCE_DIR/logs/gc.log";
	
	&echo_msg( " 8.	LogGC Output\n");
	&echo_msg( " Current Value		: $wsadmin_loggc_output\n");
	&echo_msg( " Recommended Value	: $wsadmin_new_loggc_output\n\n ");
	
	$wsadmin_server_mode=getWSJVMOption("-server","flag"," ");
	$wsadmin_new_server_mode="-server";
	
	&echo_msg( " 9.	JVM in Server Mode\n");
	&echo_msg( " Current value		: $wsadmin_server_mode\n");
	&echo_msg( " Recommended value	: $wsadmin_new_server_mode\n\n");
	
	$wsadmin_stack_size=getWSJVMOption("-Xss","flag"," ");
	if($JVM64bitAvailable eq true)
	{
		$wsadmin_new_stack_size="-Xss"."${AMTUNE_PER_THREAD_STACK_SIZE_IN_KB_64_BIT}"."k";
	}
	else
	{
		$wsadmin_new_stack_size="-Xss"."${AMTUNE_PER_THREAD_STACK_SIZE_IN_KB}"."k";
	}
	
	&echo_msg( " 10.	JVM Stack Size\n");
	&echo_msg( " Current value		: $wsadmin_stack_size\n");
	&echo_msg( " Recommended value	: $wsadmin_new_stack_size\n\n");
	
	$wsadmin_new_size=getWSJVMOption("-XX:NewSize","flag"," ");
	$wsadmin_new_new_size="-XX:NewSize=${maxNewSize}"."M";
	
	&echo_msg( " 11. 	New Size\n");
	&echo_msg( " Current value		: $wsadmin_new_size\n");
	&echo_msg( " Recommended value 	: $wsadmin_new_new_size\n\n");
	
	$wsadmin_max_new_size=getWSJVMOption("-XX:MaxNewSize","flag", " ");
	$wsadmin_new_max_new_size="-XX:MaxNewSize=${maxNewSize}"."M";
	
	&echo_msg( " 12. 	Max New Size\n");
	&echo_msg( " Current value		: $wsadmin_max_new_size\n");
	&echo_msg( " Recommended value	: $wsadmin_new_max_new_size\n\n");
	
	$wsadmin_disable_explitcit_gc=getWSJVMOption("-XX:+DisableExplicitGC","flag"," ");
	$wsadmin_new_disable_explitcit_gc="-XX:+DisableExplicitGC";
	
	&echo_msg( " 13. 	Disable Explicit\n");
	&echo_msg( " Current value		: $wsadmin_disable_explitcit_gc\n");
	&echo_msg( " Recommended value 	: $wsadmin_new_disable_explitcit_gc\n\n");
	
	$wsadmin_use_parallel_gc=getWSJVMOption("-XX:+UseParNewGC","flag"," ");
	$wsadmin_new_use_parallel_gc="-XX:+UseParNewGC";
	
	&echo_msg( " 14. 	Use Parallel GC\n");
	&echo_msg( " Current value		: $wsadmin_use_parallel_gc\n");
	&echo_msg( " Recommended value	: $wsadmin_new_use_parallel_gc\n\n");
	
	$wsadmin_print_class_histogram=getWSJVMOption("-XX:+PrintClassHistogram","flag"," ");
	$wsadmin_new_print_class_histogram="-XX:+PrintClassHistogram";
	
	&echo_msg( " 15. 	Print Class Histogram\n");
	&echo_msg( " Current value		: $wsamdin_print_class_histogram\n");
	&echo_msg( " Recommended value 	: $wsadmin_new_print_class_histogram\n\n");
	
	$wsadmin_print_gc_time_stamps=getWSJVMOption("-XX:+PrintGSTimeStamps","flag"," ");
	$wsadmin_new_print_gc_time_stamps="-XX:+PrintGCTimeStamps";
	
	&echo_msg( " 16. 	Print GC Time Stamps\n");
	&echo_msg( " Current value		: $wsadmin_print_gc_time_stamps\n");
	&echo_msg( " Recommended value	: $wsadmin_new_print_gc_time_stamps\n\n");
	
	$wsadmin_use_con_mark_sweep_gc=getWSJVMOption("-XX:+UseConcMarkSweepGC","flag"," ");
	$wsadmin_new_use_con_mark_sweep_gc="-XX:+UseConcMarkSweepGC";
	
	&echo_msg( " 17. 	Enable Concurrent Mark Sweep GC\n");
	&echo_msg( " Current value 		: $wsamdin_use_conc_mark_sweep_gc\n");
	&echo_msg( " Recommended value	: $wsadmin_new_use_con_mark_sweep_gc\n\n");
	
	if($AMTUNE_MODE eq REVIEW)
	{
		return;
	}
	
	# Call backup function in amtune-utils.pl bacause it does not use backup functions in amutils
	# Configuration file cannot be backup in the same directory because deploy-config will fail 
	
	
	&backupConfigFile($tune_file,"$ENV{TEMP}/config-ws7-backup");
	#Start: Performance Related Thread Pool parameters for OpenSSO
	if( $JVM64bitAvailable eq true)
	{
		$WSADMIN_OPTION="set-thread-pool-prop";
		$WSADMIN_OPTION_MIN="min_threads=$AMTUNE_NUM_WS_MIN_THREADS";
		$WSADMIN_OPTION_MAX="max_threads=$numOfMaxThreadPool";
		$WSADMIN_OPTION_QUEUE="queue-size=$AMTUNE_NUM_TCP_CONN_SIZE";
		$WSADMIN_OPTION_STACK="stack-size=$AMTUNE_NATIVE_STACK_SIZE_64_BIT";
		@args=($WSADMIN,$WSADMIN_OPTION,@WSADMIN_COMMON_PARAMS,$WSADMIN_OPTION_MIN,$WSADMIN_OPTION_MAX,$WSADMIN_OPTION_QUEUE,$WSADMIN_OPTION_STACK);
		system(@args)==0 or die "\n Error executing command @args\n";
	}
	else
	{
		$WSADMIN_OPTION="set-thread-pool-prop";
		$WSADMIN_OPTION_MIN="min_threads=$AMTUNE_NUM_WS_MIN_THREADS";
		$WSADMIN_OPTION_MAX="max_threads=$numOfMaxThreadPool";
		$WSADMIN_OPTION_QUEUE="queue-size=$AMTUNE_NUM_TCP_CONN_SIZE";
		@args=($WSADMIN,$WSADMIN_OPTION,@WSADMIN_COMMON_PARAMS,$WSADMIN_OPTION_MIN,$WSADMIN_OPTION_MAX,$WSADMIN_OPTION_QUEUE);
		system(@args)==0 or die "\n Error executing command @args\n";
	}
	
	#End : Performance Related Thread Pool parameters for OpenSSO
	#Start : Performance Related Http Listener parameters for OpenSSO
	$WSADMIN_OPTION_HTTP_LISTENERS="set-http-listener-prop";
	$WSADMIN_HTTP_LISTENER_VARIABLE="--http-listener=$WSADMIN_HTTPLISTENER";
	$WSADMIN_OPTION_ACCEPTORTHREADS="acceptor-threads=$acceptorThreads";
	@args=($WSADMIN,$WSADMIN_OPTION_HTTP_LISTENERS,@WSADMIN_COMMON_PARAMS,$WSADMIN_HTTP_LISTENER_VARIABLE,$WSADMIN_OPTION_ACCEPTORTHREADS);
	system(@args)==0 or die "\n Error executing command @args\n";
	
	#End : Performance Related Http Listener parameters for OpenSSO
	
	
	#Start : Performance Related JVM Options for OpenSSO
	$curJvmOptionHeapString="";
	$curJvmOptionHeapString=&createParamString($curJvmOptionHeapString,"$wsadmin_min_heap");
	$curJvmOptionHeapString=&createParamString($curJvmOptionHeapString,"$wsadmin_max_heap");
	chomp($curJvmOptionHeapString);
	if($curJvmOptionHeapString ne "")
	{
		# Delete Min Heap and Max Heap JVM Option : -Xms -Xmx
		deleteJVMOptionUsingWSAdmin($curJvmOptionHeapString,"combined");
	}
	
	$curJvmOptionString="";
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_server_mode");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_stack_size");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_loggc_output");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_new_size");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_max_new_size");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_disable_explitcit_gc");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_use_parallel_gc");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_print_class_histogram");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_print_gc_time_stamps");
	$curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_override_default_libthread");
        $curJvmOptionString=&createParamString($curJvmOptionString,"$wsadmin_use_con_mark_sweep_gc");
	chomp($curJvmOptionString);				
	
	if($curJvmOptionString ne "")
	{
		#Delete other JVM Options
		deleteJVMOptionUsingWSAdmin($curJvmOptionstring)
	}
	#End : Performance Related JVM Options for OpenSSO
	
	#Start : Performance Related JVM Options for OpenSSO
	#Insert Min Heap and Max Heap JVM Option : -Xms -Xmx 
	
	$jvm_insert_result1=&insertJVMOptionUsingWSAdmin("$wsadmin_new_min_heap $wsadmin_new_max_heap","combined"); 
	
	if($jvm_insert_result1 == 0)
	{
		&echo_msg( "Insert JVM option command executed successfully\n");
	}
	else
	{
		&echo_msg( "Insert JVM option command execution failed\n");
	}
		    	
	$newJvmOptionString="$newJvmOptionString $wsadmin_new_server_mode";
	$newJvmOptionString="$newJvmOptionString $wsadmin_new_stack_size";
	$newJvmOptionString="$newJvmOptionString $wsadmin_new_loggc_output";
	$newJvmOptionString="$newJvmOptionString $wsadmin_new_new_size";
	$newJvmOptionString="$newJvmOptionString $wsadmin_new_max_new_size";
	$newJvmOptionString="$newJvmOptionString $wsadmin_new_disable_explitcit_gc";
	$newJvmOptionString="$newJvmOptionString $wsadmin_new_use_parallel_gc";
	$newJvmOptionString="$newJvmOptionString $wsadmin_new_print_class_histogram";
    $newJvmOptionString="$newJvmOptionString $wsadmin_new_print_gc_time_stamps";
   	   	
    #Insert other JVM Options
    $jvm_insert_result2=&insertJVMOptionUsingWSAdmin($newJvmOptionString);
    if($jvm_insert_result2 == 0)
	{
		&echo_msg( "Insert JVM option command executed successfully\n");
	}
	else
	{
		&echo_msg( "Insert JVM option command execution failed\n");
	}
	
    	
    ##End : Performance Related JVM Options for OpenSSO
    	
    #If one of the insert operation is successful, execute deploy-config operation 
    	
    if(($jvm_insert_result1 == 0) || ($jvm_insert_result2 == 0))
    {
    	&echo_msg( "Deploying the configuration...\n");
    	$WSADMIN_OPTION_DEPLOY="deploy-config";
    	@args=($WSADMIN,$WSADMIN_OPTION_DEPLOY,@WSADMIN_COMMON_PARAMS_NO_CONFIG,$WSADMIN_CONFIG);
		system(@args);
		&echo_msg( "Server restart. Please wait...\n");
    	$WSADMIN_OPTION_RESTART="restart-instance";
    	@args=($WSADMIN,$WSADMIN_OPTION_RESTART,@WSADMIN_COMMON_PARAMS);
    	system(@args);
    }
}

#############################################################################
# Start of main program
#############################################################################

#import the environment
if(-f "$SCRIPT_LOCATION/amtune-env.pl")
{
	if($INIT_STATUS ne INIT_COMPLETE)
	{	
		$PERL_CMD="perl";
		$SCRIPT="$SCRIPT_LOCATION\amtune-env.pl";
		@args=("$PERL_CMD","$SCRIPT");
		system(@args)==0 or die "\n Error executing command @args\n";
		#`perl $SCRIPT_LOCATION\amtune-env.pl`;
	}
}

open(FP,">$WSADMIN_PASSFILE");
print FP "$WSADMIN_PASSWORD_SYNTAX$WSADMIN_PASSWORD";
close(FP);

&echo_msg("\nOpenSSO - Web Server Tuning Script\n");

tuneServerXML;

&echo_msg("$PARA_SEP\n");

