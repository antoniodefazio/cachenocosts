# How to create a zero cost distributed cache that works on all managed Kubernetes services: Openshift, GKE, EKS and so on, via Jgroups Java library in conjunction with Infinispan inside a Spring Boot project

I worked for the most important Italian telecommunications company, the manager tells me that during a migration we have to manage some data with a distributed and shared cache across the various pods. My proposal "Let's use Redis!", the manager's response "the budget does not include the introduction of another infrastructure". Then I thought "Actually nothing in life comes for free..."

I start with in-depth questions about the task assigned to me and do a small analysis of the requirements and a minimum feasibility study of my solution starting my research. I also point out to the manager that we are in the migration phase so we need to use a solution that works for both Google Kuberntes Engine and Openshift. **From the analysis I discover that the cache was used to exchande data and to load some data from database which rarely change, and when they change I can invoke eviction.**

I discover that exists an open source toolkit for reliable messaging written in Java: JGroups (www.jgroups.org) , and it can be used under the covers to send messages back and forth between other application servers, optimizing network communication and resource usage for distributed applications. Furthermore, JGroups and Infinispan(https://infinispan.org/) together can create a distributed cache where every pod/application is a node, because JGroups provides multicast-based group communication. The most important object of JGroups is JChannel(http://www.jgroups.org/javadoc3/org/jgroups/JChannel.html) which serves as the backbone of communication, it is able to send messages to all node registered.      

Now let's venture into the project first locally and then in the cloud

Brief description of the project:

The project exposes the simple REST APIs documentation via Swagger to http://localhost:8080/swagger-ui/index.html#:

-/infinispanhit/, inserts value in cache with a specified key, it can be run in a node and call get in another node, to verify that they are aligned 

-/infinispanget/, gets value from cach with a specified key, it can be run at two different nodes to verify that they are aligned

-/hitCached, inside the Service the @Cacheable is used to cache database data

-/all-caches, fetches all caches(distributed and not) and their data can be run at two different nodes to verify that they are aligned




## 1) Steps to test the app locally

The app launched on localhost uses the file https://github.com/antoniodefazio/cachenocosts/blob/master/src/main/resources/jgroups-udp.xml as a JGroups configuration, therefore it uses the UDP multicast ports used for broadcasting messages to multiple hosts within a network, that is IGMP (Internet Group Management Protocol) described in
RFC 3376(https://datatracker.ietf.org/doc/html/rfc3376)


- run the Spring Boot and hit Swagger at http://localhost:8080/swagger-ui/index.html#
- curl http://localhost:8080/infinispanhit/3 to insert the value in cache with key “3”
- curl http://localhost:8080/infinispanget/3 to get the value in cache with key “3”
- run the Spring Boot on 8081 port hit Swagger at http://localhost:8081/swagger-ui/index.html#
Now we have 2 Spring Boot running on different ports, 2 JVM, no insert in cache called on 8081 app,  so the final test is:
- curl http://localhost:8081/infinispanget/3 to get the value in cache with key “3” and  verify that they are aligned with 8080 app

## 2) Steps to test the app locally with Docker Desktop(https://www.docker.com/products/docker-desktop)

Docker Desktop introduced the ability to use Kubernetes as an orchestration tool in version 18.02, released in February 2018 so after install Docker Desktop you can enable K8S to get a cluster running on localhost.

- locally build the docker image at https://github.com/antoniodefazio/cachenocosts/blob/master/Dockerfile, it is a 2 phase build optimized caching the Maven .m2 folder. I also installed curl and apache2-utils for our tests within the pods.
- kubectl apply the https://github.com/antoniodefazio/cachenocosts/blob/master/k8s/noserver-k8s-objects.yaml

(I underline that there are 2 replicas, therefore we will have **2 pods running with Deployment name cachenoserver**)

**The very intriguing thing is always to create Kubernetes objects and solve the whole scenario...**

This way the pods will use https://github.com/antoniodefazio/cachenocosts/blob/master/src/main/resources/jgroups-kubernetes-kube-ping.xml as the JGroups configuration, so we will use the Jgroups KUBE_PING protocol(https://github.com/jgroups-extras/jgroups-kubernetes). In a nutshell, this exploits the power of Kuberntes labels as pod selectors which can be searched for and registered as cache nodes. In summary: in order for a pods to call the K8S API to know which pods have a certain label it needs the privileges to do so, as in K8S RBAC is in force each pod needs an associated ServiceAccount with the privileges to make the call. By associating the ServiceAccount with the pod, K8S places the token with privileges in the path /var/run/secrets/kubernetes.io/serviceaccount/token

But let's get practical to understand better: ssh into the pod and launch the following commands in sequence:

Point to the internal API server hostname

APISERVER="https://${KUBERNETES_SERVICE_HOST}:${KUBERNETES_SERVICE_PORT}" 

Path to ServiceAccount token

SERVICEACCOUNT=/var/run/secrets/kubernetes.io/serviceaccount

Read this Pod's namespace

NAMESPACE=$(cat ${SERVICEACCOUNT}/namespace)

Read the ServiceAccount bearer token

TOKEN=$(cat ${SERVICEACCOUNT}/token)

Reference the internal certificate authority (CA)

CACERT=${SERVICEACCOUNT}/ca.crt


Now let's use curl to make the call we talked about above:

curl --cacert ${CACERT} --header "Authorization: Bearer ${TOKEN}" -X GET ${APISERVER}/api/v1/namesp
aces/${NAMESPACE}/pods?labelSelector=app%3Dcachenoserver

**The result is the list of running pods with label app: cachenoserver**

Now, thanks to the K8S LoadBalancer

apiVersion: v1
kind: Service
metadata:
  name: cachenoserver-loadbalancer
spec:
  type: LoadBalancer
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
  selector:
    app: cachenoserver

**which esposes the 8080 port to the cluster** we can hit the same http://localhost:8080/swagger-ui/index.html#, but remembering that we are now aiming for pods(2) running in our local k8s.

So:

- curl http://localhost:8080/infinispanhit/3 to insert the value in cache with key “3”
- to now test using get that the 2 pods are actually aligned we must **ensure that the LoadBalancer balancing algorithm queries both** and in this regard I have prepared a Jmeter(https://jmeter.apache.org/) file at https://github.com/antoniodefazio/cachenocosts/blob/master/k8s/HTTP_Request.jmx which allows you to make thousands of simultaneous calls following which you can **check the logs** you can see how both pods contain the same data in cache

Now comes the fun part:

- kubectl apply the https://github.com/antoniodefazio/cachenocosts/blob/master/k8s/noserver2-k8s-objects.yaml

(I underline that now there are another 2 replicas of different pods, therefore we will now have total 4 pods running labeled app: cachenoserver)

Now, thanks to the K8S LoadBalancer

apiVersion: v1
kind: Service
metadata:
  name: cachenoserver2-loadbalancer
spec:
  type: LoadBalancer
  ports:
  - port: 8081
    targetPort: 8080
    protocol: TCP
  selector:
    app2: cachenoserver2

**which esposes the 8081 port to the cluster** we can hit the same http://localhost:8081/swagger-ui/index.html#, but remembering that we are now aiming for pods(2) running in our local k8s.

- curl http://localhost:8081/infinispanget/3 to get the value in cache with key “3” and  verify that they are aligned with 8080 app


## 3) Steps to test the in Cloud on  Openshift, GKE, EKS and so on

- kubectl apply the https://github.com/antoniodefazio/cachenocosts/blob/master/k8s/remote-noserver-k8s-objects.yaml

It is the same for local K8S, except for LoadBalancer creations as we know that for k8s no differences between cloud and onprem, the only difference is in the integration points like LoadBalancer. **The same is also the Infinispan and related Jgroups configuration but I just wanted to keep separated files for Cloud** and underline, example, that in

 <org.jgroups.protocols.kubernetes.KUBE_PING
     port_range="10"
     namespace="${POD_NAMESPACE:antoniodefazio-dev}"
     labels="${KUBERNETES_LABELS:app=cachenoserver}"
   />
   
the namespace is no more default so you can choose one and in Openshift, example, is the name of the project.

So:

- kubectl apply the https://github.com/antoniodefazio/cachenocosts/blob/master/k8s/remote-noserver2-k8s-objects2.yaml

(I underline that now there are another 2 replicas of different pods, therefore we will now have in Cloud K8Stotal 4 pods running labeled app: cachenoserver)

Now we don t have any LoadBalancer and any external access to test the cache I decide to use Apache Benchmark(https://httpd.apache.org/docs/2.4/programs/ab.html) within the pods, so after ssh into pod with **Deployment name cachenoserver**(remember that the pod cachenoserver2 are the second ones):

- curl http://localhost:8080/infinispanhit/3, to save in cache

Inside one of these pods(**cachenoserver**) we hit the “others”(**cachenoserver2**) via service discovery: 

- curl cachenoserver2-service:8080/infinispanget/3
- ab -n 1000 cachenoserver2-service:8080/infinispanget/3

Apache Benchmark allows you to make 1000 simultaneous calls to make sure you query all the pods "behind" a Service…..
