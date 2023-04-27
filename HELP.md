## Tidal Object Documentation

Tidal data objects all have a modified date but not all objects have a created date
SOme of these have a cached date but again, not going to be used.
Most but not all have an ownership association , two examples are fiscal and images, again none of these are going to be used
Queue for example does not have owner but modified. Again this will be a reference object not a object to be create
but we need to code for future where we may want to build queues
Actions object in TIDAL had neither but I don't think it's a part of the main objects.

Modified Object
modified date

Owner Object
owner_id
owner_name

Job/Group Object - these share many elements / fields of data and are abstract on many levels.
However, it's only data elements but not type. Group and hold groups and jobs but job just a job
Job however can be many types sharing 99% of all the same data with the exemption of what that job type needs


Abstract JobGroup Object -> extends owner object
shared elements
id
create date
name
Calendar
class
start time
end time
agent
rerun
List<AbstractDependency> - List of dependencies

Job Group -> extends abstract jobGroupObject
List<JobGroupObject> = Jobs or groups

Job -> extends abstract jobGroupObject
command
params
working dir

FTPJob -> extends abstract jobGroupObject
ftp host
ftp user
- In tidal this is a single object where you must know how things work.
- Example: if this is a plan FTP or FTPS protocol then you do not have the option to set authen to user/pass or private key file
- Then for each protocol you have the operations
- list,make,delete only has remote path
- put and get have file name, local path, remote path and new file name
- mput and mget have all the same of put and get but not new file name
- mdelete has similar to mput / mget but only only file name and remote path



Abstract Dependency - There are three types of dependency (Job/Group,file and variable)
job/group id -> what job/group does this dependency belong to

File Dep
file name
file operation

Job Dep
dep on jobid
dep type -> Completed normal , abnormal

Var dep
variable id
variable value
dep type -> Contains, not contain, like , etc. 
	