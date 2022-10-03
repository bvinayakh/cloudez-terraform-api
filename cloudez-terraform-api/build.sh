./mvnw -DskipTests package

commit=$(git log -1 --pretty=%h)
region=us-west-2
account=140199734014
repourl=${account}.dkr.ecr.${region}.amazonaws.com
repo=cicd-images
service=cloudez-reporting-api

aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${repourl}
docker build -t ${service} .
docker tag ${service} ${account}.dkr.ecr.us-west-2.amazonaws.com/${repo}:${service}-master-${commit}
docker push ${account}.dkr.ecr.${region}.amazonaws.com/${repo}:${service}-master-${commit}
docker rmi ${repourl}/${repo}:${service}-master-${commit}

docker rmi $(docker images --filter "dangling=true" -q --no-trunc)
