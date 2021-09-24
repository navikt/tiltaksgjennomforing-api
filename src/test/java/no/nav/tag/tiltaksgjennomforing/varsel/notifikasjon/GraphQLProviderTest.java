package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import org.junit.Before;
import org.junit.Test;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class GraphQLProviderTest {

    String schema = "schema {\n" +
            "  mutation: Mutation\n" +
            "  query: Query\n" +
            "}\n" +
            "\n" +
            "type Query {\n" +
            "  myName: String\n" +
            "}\n" +
            "\n" +
            "type Mutation {\n" +
            "    OpprettNyBeskjed(eksternId: String!, virksomhetsnummer: String!, lenke: String!, tekst: String!) : NyBeskjedInput\n" +
            "}\n" +
            "\n" +
            "type NyBeskjedInput {\n" +
            "    mottaker: MottakrInput!\n" +
            "    notifikasjon: NotifikasjonInput!\n" +
            "    metadata: MetadataInput!\n" +
            "}\n" +
            "\n" +
            "type MottakrInput {\n" +
            "    altinn: AltinnMottakerInput!\n" +
            "}\n" +
            "\n" +
            "type NotifikasjonInput {\n" +
            "    merkelapp: String!\n" +
            "    tekst: String!\n" +
            "    lenke: String!\n" +
            "}\n" +
            "\n" +
            "type MetadataInput {\n" +
            "    eksternId: String!\n" +
            "    opprettetTidspunkt: String\n" +
            "    grupperingsid: String\n" +
            "}\n" +
            "\n" +
            "type AltinnMottakerInput {\n" +
            "    serviceCode: String!\n" +
            "    serviceEdition: String!\n" +
            "    virksomhetsnummer: String!\n" +
            "}\n";


    @Before
    public void init() {}

    @Test
    public void schemaParserTest() {
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        RuntimeWiring runtimeWiring = newRuntimeWiring()
                .type("Mutation", builder -> builder.dataFetcher("eksternId", new StaticDataFetcher("123123")))
                .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);


    }
}
