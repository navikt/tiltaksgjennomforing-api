package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({ Miljø.LOCAL, "wiremock"})
public class GraphQLProviderTest {
    @Autowired
    GraphQLProvider graphQLProvider;



    @Before
    public void init() {}

    @Test
    public void schemaParserTest() throws IOException {
        graphQLProvider.init();
        GraphQL graphQL = graphQLProvider.graphQL(); // bookById
        graphQLProvider.schemaResource.toString();

/*        Map<String, String> root = new HashMap<>();
        root.put("bookById", "book-1");
        String query = "query { bookById(id: ID) { id name pageCount author { id firstName lastName } } }";

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .root(root)
                .query(query)
                .build();
        ExecutionResult executionResult = graphQL.execute(executionInput);


        Object data = executionResult.getData();
        List<GraphQLError> errors = executionResult.getErrors();*/
    /*    ExecutionResult executionResult = build.execute(executionInput);
        Object data = executionResult.getData();

        if(data != null) {
            System.out.println(data.toString());
        }*/

    }
}
