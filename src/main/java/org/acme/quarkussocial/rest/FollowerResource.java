package org.acme.quarkussocial.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.quarkussocial.domain.model.Follower;
import org.acme.quarkussocial.domain.repository.FollowerRepository;
import org.acme.quarkussocial.domain.repository.UserRepository;
import org.acme.quarkussocial.dto.FollowerRequest;
import org.acme.quarkussocial.dto.FollowerResponse;
import org.acme.quarkussocial.dto.FollowersPerUserResponse;

import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository followerRepository;
    private UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository followerRepository, UserRepository userRepository){
        this.userRepository = userRepository;
        this.followerRepository = followerRepository;
    }

    @Transactional
    @PUT
    public Response followUser(@PathParam("userId")Long userId, FollowerRequest request){

        if(userId.equals(request.getFollowerId())){
            return Response.status(Response.Status.CONFLICT)
                    .entity("You can't follow yourself! Try to follow another user.")
                    .build();
        }

        var user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        var follower = userRepository.findById(request.getFollowerId());

        boolean follows = followerRepository.follows(follower, user);

        if(!follows){
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);
            followerRepository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();

    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId){

        var user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

         var followerList = followerRepository.findByUser(userId);
        FollowersPerUserResponse followerResponseObject = new FollowersPerUserResponse();
        followerResponseObject.setFollowersCount(followerList.size());

        var followersCompleteList = followerList.stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());

        followerResponseObject.setContent(followersCompleteList);
        return Response.ok(followerResponseObject).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser(
            @PathParam("userId") Long userId,
            @QueryParam("followerId") Long followerId){

        var user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        followerRepository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).entity(followerId).build();

    }
}
