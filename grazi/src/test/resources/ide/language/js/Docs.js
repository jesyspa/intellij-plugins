/**
 * Module description <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning>
 * @module ExampleClassWithNoTypos
 */

/**
 * A group of *members*.
 *
 * This class has no useful logic; it's just a documentation example.
 */
class ExampleClassWithNoTypos {
    /**
     * Creates an empty group
     * @param  {String} name the name of the group
     */
    constructor(name) {
        /** @private */
        this.name = name;
    }

    /**
     * Adds a [member] to this group.
     * @param {String} member member to add
     * @return {Number} the new size of the group.
     */
    goodFunction(member) {
        return 1; // no error comment
    }
}

/**
 * It is <warning descr="ARTICLE_MISSING">friend</warning>
 *
 * <warning descr="PLURAL_VERB_AFTER_THIS">This guy have</warning> no useful logic; it's just a documentation example.
 */
class ExampleClassWithTypos {
    /**
     * Creates an empty group
     * @param  {String} name the <warning descr="COMMA_WHICH">name which</warning> group
     */
    constructor(name) {
        /** @private */
        this.name = name;
    }

    /**
     * It <warning descr="IT_VBZ">add</warning> a [member] to this <warning descr="MORFOLOGIK_RULE_EN_US">grooup</warning>.
     * @param {String} member member to add
     * @return {Number} the new size <warning descr="DT_DT">a the</warning> group.
     */
    badFunction(member) {
        return 1; // It <warning descr="IT_VBZ">are</warning> <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning> comment
    }
}

module.exports = ExampleClassWithNoTypos;
