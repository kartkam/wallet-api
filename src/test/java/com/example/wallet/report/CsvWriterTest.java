package com.example.wallet.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class CsvWriterTest {

    @Test
    void writesHeaderAndRowsWithCrlfLineEndings() {
        String csv = CsvWriter.write(List.of("id", "name"), List.of(List.of("1", "alice"), List.of("2", "bob")));

        assertThat(csv).isEqualTo("id,name\r\n1,alice\r\n2,bob\r\n");
    }

    @Test
    void quotesFieldsContainingSeparatorsQuotesOrNewlines() {
        assertThat(CsvWriter.escape("a,b")).isEqualTo("\"a,b\"");
        assertThat(CsvWriter.escape("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
        assertThat(CsvWriter.escape("line1\nline2")).isEqualTo("\"line1\nline2\"");
    }

    @Test
    void leavesPlainFieldsUntouched() {
        assertThat(CsvWriter.escape("plain")).isEqualTo("plain");
        assertThat(CsvWriter.escape("12.50")).isEqualTo("12.50");
    }

    @Test
    void writesNullAsAnEmptyField() {
        String csv = CsvWriter.write(List.of("a", "b"), List.of(Arrays.asList("x", null)));

        assertThat(csv).isEqualTo("a,b\r\nx,\r\n");
    }

    @Test
    void writesOnlyTheHeaderWhenThereAreNoRows() {
        assertThat(CsvWriter.write(List.of("a"), List.of())).isEqualTo("a\r\n");
    }
}
