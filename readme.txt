HOW THIS CODE WORKS
These lines are meant for the purpose of explaining how to use this program.
The "INPUT FILE" section describes how to create files intended to provide data to be processed.
The section "MAIN" explain how this program process data.
Finally, the last section "RESULT" explain how to get the final result.

--- INPUT FILES
    These are files that provide input data to the code.

    The file named "data.txt" contains the list of resources to be outsourced.
    The syntax for this file is :
        {resource_1, ..., resource_n}

    The file named "attributes.txt" contains the list of attributes and for each attribute is defined the list of values
    that it can assume, including the value "-".
    The syntax for this file is :
        attribute_name_1 = {value_1, ..., value_n}
            ...
        attribute_name_k = {value_1, ..., value_m}

    The file named "plans.txt" contains the list of plans with their parameters (a value for each attribute).
    The syntax for this file is :
        piano_1 = {value_1, ..., value_n}
            ...
        piano_k = {value_1, ..., value_m}

    The ". /rules" folder contains a file for each original copy or copy of a resource you want to define.
    For each original copy and for each resource copy a list of rules and terms must be defined.
    The syntax for file of R_o type is :
        R_o(resource_j) = {
        attribute_name = (value_1, ..., value_n),
            ...
        attribute_name = (value_1, ..., value_m),
        type_of_rule = ({term_1, ..., term_o}),
            ...
        type_of_rule = ({value_1, ..., term_p})
        }
    The syntax for file of R_c type is :
        R_ci(resource_j) = {
        attribute_name = (value_1, ..., value_n),
            ...
        attribute_name = (value_1, ..., value_m),
        type_of_rule = ({term_1, ..., term_o}),
            ...
        type_of_rule = ({term_1, ..., term_p})
        }
    To negate a term is necessary add "!" before the attribute name.

    The file "global_rules.txt" contains the list of global requirements.
    The syntax for this file is :
        ALL_TOGETHER*(resource, resource);
            ...
        NOT_TOGETHER(resource, resource);
            ...
        NOT_TOGETHER*(resource, resource);
            ...
        ALL_SPLIT(resource);
            ...
        ALL_SPLIT(resource);
            ...
        SPLIT(resource);
            ...
        ALONE(resource);
            ...
        ALONE(resource);

    The file "price.txt" contains the cost per GB for each plan.
    The syntax of this file is :
        p_1={n}
            ...
        p_k={m}

    The file "space.txt" contains the storage space required in GB for each resource.
    The syntax of this file is :
        resource={n}
            ...
        resource={m}

MAIN
    This is the main class where all structures are defined and all inputs are stored.
    This program first reads the input files and stores the read data.
    Then identify acceptable plans for each original copy or copy of a resource.
    Acceptable plans are those plans that meet the basic requirements of a resource.
    Finally, it is called the "write" method that generates the python file which allows you to identify plans for
    effective allocation.

RESULT
    To find the result using the SAT-Solver
    	1. Install Google’s OR-Tools at the link : https://developers.google.com/optimization/install
    	2. Open CMD and paste the following commands
    		cd .\python\
    		python result.py