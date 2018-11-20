import com.google.protobuf.FieldMask
import com.google.protobuf.Timestamp
import com.google.protobuf.util.FieldMaskUtil
import com.google.protobuf.util.Timestamps
import me.lecoding.grpclearning.api.UserOuterClass
import spock.lang.Specification

class FieldMaskSpec  extends Specification{

    def "测试fieldMask"(){
        given:"user and mask"
        UserOuterClass.User u =UserOuterClass.User.newBuilder()
                .setUsername("jack")
                .setNickname("jack nil")
                .setEmail("jack@example.com")
                .setCreateTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .setId(UUID.randomUUID().toString()).build()
        FieldMask mask = FieldMask.newBuilder().addPaths("username").addPaths("nickname").build()
        UserOuterClass.User.Builder merge = UserOuterClass.User.newBuilder()
        when:"merge"
        FieldMaskUtil.merge(mask,u,merge)
        UserOuterClass.User mergeUser =merge.build()

        then:"request.fieldmask valid"
        mergeUser.getEmail() == ""
        mergeUser.getCreateTime() == Timestamp.getDefaultInstance()
        mergeUser.getId() == ""
    }
}
