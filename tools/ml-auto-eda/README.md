# Auto Data Exploration and Feature Recommendation Tool

Machine learning (ML) projects typically start with a comprehensive exploration of the provided datasets.
It is critical that ML practitioners to gain a deep understanding of:
- the properties of the data.
- the quality of the data.
- the potential predictive power of the data.
This process lays the groundwork for the subsequent feature selection and engineering steps, and it provides a solid
foundation for building good ML models. Exploratory Data Analysis (EDA), feature selection, and engineering are often
tied together and are important steps of the ML journey.

The objective of this tool is to perform comprehensive EDA, which includes
- Descriptive analysis of each attribute in a dataset for numerical, categorical
- Correlation analysis of two attributes (numerical versus numerical, numerical versus categorical,
and categorical versus categorical) through qualitative and/or quantitative analysis.

Based on the EDA performed, feature recommendations are made, and a summary report will be generated. A snapshot of the
sample report is as follows:

<img align=center src="./ml_eda/reporting/templates/report_snapshot.png" alt="Sample Report" width="600px"/>


Google Cloud tools used:
- [Google Cloud Platform](https://cloud.google.com/) (GCP) lets you build and
host applications and websites, store data, and analyze data on Google's
scalable infrastructure.
- [Google BigQuery](https://cloud.google.com/bigquery/) A fast, highly scalable,
cost-effective, and fully managed cloud data warehouse for analytics, with even
built-in machine learning.

## Use the tool
```shell
bash run.sh \
    --key_file [KEY_FILE] \
    --data_source [DATA_SOURCE] \
    --preprocessing_backend [BACK_END] \
    --metadata [META_FILE] \
    --bq_table [BQ_TABLE] \
    --target_name [TARGET_ATTRIBUTE] \
    --ml_type [ML_TYPE] \
    --generated_metadata [TO_GENERATE] \
    --report_path [REPORT_PATH]
```
where:

- KEY_FILE, **optional**, string: Key file of the service account used to authenticate to the BigQuery API. If this is
not specified, the `GOOGLE_APPLICATION_CREDENTIALS` from the environment variable will be used.
- DATA_SOURCE, **optional**, enum: Type of data source containing the training data. **Currently only support `BIGQUERY`**
- BACK_END, **optional**, enum: Analysis computation backend. **Currently only support `BIGQUERY`**
- META_FILE, string: Configurtion file containing the description of the datasource, and configurations of analysis
- BQ_TABLE, string: BigQuery table name to be analyzed, in the format of [project.dataset.table]
- TARGET_ATTRIBUTE, **optional**, string: Name of attribute acting as target (label) in a ML problem
- ML_TYPE, **optional**, enum: Type of machine learning problem, either `Classification` or `Regression`
- TO_GENERATE, **optional**, boolean: Indicates whether the medata file should be regenerated from the datasource.
If this is true, the generated `metadata.ini` will be saved at the path specified in `META_FILE`
- REPORT_PATH, **optional**, string: Path for storing generated report

One example is as follows:
```shell
bash run.sh \
    --generate_metadata True \
    --bq_table bigquery-public-data.ml_datasets.census_adult_income \
    --target_name race
```

#### Remarks
- `TARGET_ATTRIBUTE` and `ML_TYPE` will only take effect while generating `metadata.ini` from the datasource, i.e.,
`TO_GENERATE` is set `True`

## Customize metadata.ini
It is possible to customize the `metadata.ini` if you want to
- Specify which attributes to analyze
- Control what analysis to performed
- Tune parameters for certain analysis

One example of the `metadata.ini` is as follows:
```ini
[DATASOURCE]
Type=BIGQUERY
Location=bigquery-public-data.ml_datasets.census_adult_income

[SCHEMA]
Target=race
NumericalFeatures=age,hours_per_week,functional_weight
CategoricalFeatures=marital_status,race,education,native_country

[ANALYSIS.RUN]
# Control what analysis to run. By default, descriptive, histogram, value_counts, pearson_correlation
# will run. Others will be triggered by configuration.

# Qualitative Analysis
CONTINGENCY_TABLE.Run=True
TABLE_DESCRIPTIVE.Run=True

# Quantitative Analysis
PEARSON_CORRELATION.Run=True
INFORMATION_GAIN.Run=True
CHI_SQUARE.Run=True
ANOVA.Run=True

[ANALYSIS.CONFIG]
General.CardinalityLimit=20
HISTOGRAM.Bin=10
VALUE_COUNTS.Limit=15
```

#### Remarks
- A base version of the `metadata.ini` file can be regenerated from the datasource.
And then further customization can be performed to perform desired analysis or tune parameters
- While generating `metadata.ini` from datasource, the datatype follows the attached convention.
But, this can be definitely customized after the basic version of the `metadata.ini` is generated by the problem.
    - Integer -> Numerical
    - Float -> Numerical
    - Timestamp -> Numerical
    - String -> Categorical
    - Boolean -> Categorical
- Moreover, without customization of `metadata.ini`, only the following analysis will be run
    - Descriptive Analysis
    - Pearson Correlation
    - Information Gain

## Supported Analysis
The supported analysis can be summarized here
- Descriptive Analysis
    - Missing values
    - Quantile statistics
    - Descriptive statistics
    - Distribution histogram
    - Cardinality
    - Unique counts
- Correlation Analysis
    - Qualitative Analysis
        - Contingency table
        - Descriptive table
    - Quantitative Analysis
        - Pearson correlation
        - Information gain
        - ANOVA
        - Chi-Square

## Contributors
- [Shixin Luo](https://github.com/luotigerlsx)
- [Dan Anghel](https://github.com/dan-anghel)
- [Barbara Fusinska](https://github.com/BasiaFusinska)