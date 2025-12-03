import React from "react";
import styles from "./NavCategory.module.css";
import Icon from "../../Icon";
import StyleComposer from "../../../utils/StyleComposer";

export type SubCategories = {
  flexDirection?: "row" | "column";
} & Record<string, string[]>;

type Props = {
  name: string;
  subCategories?: SubCategories | undefined;
};

function NavCategory({
  name,
  subCategories: { flexDirection, ...subCategoriesOnly } = {},
}: Props) {
  const subCategoryContainerClass = StyleComposer(styles.subCategoryContainer, {
    [styles.column as string]: flexDirection === "column",
  });

  return (
    <li key={name} className={styles.navCategory}>
      {name}
      {Object.keys(subCategoriesOnly).length > 0 ? (
        <>
          <Icon className={styles.arrow} name='chevron-down' />
          <div className={subCategoryContainerClass}>
            {Object.entries(subCategoriesOnly).map(
              ([subCategoryName, links], index) => (
                <SubCategory
                  name={subCategoryName}
                  links={links}
                  hasSeparator={index !== 0}
                />
              )
            )}
          </div>
        </>
      ) : null}
    </li>
  );
}

type SubCategoryProps = {
  name: string;
  links: string[];
  hasSeparator: boolean;
};

function SubCategory({ name, links, hasSeparator }: SubCategoryProps) {
  const className = StyleComposer(styles.subCategoryContent, {
    [styles.withSeparator as string]: hasSeparator,
  });

  return (
    <ul className={className}>
      <label className={styles.heading}>{name}</label>
      {links.map((link) => (
        <li key={link} className={styles.subCategoryLink}>{link}</li>
      ))}
    </ul>
  );
}

export default NavCategory;
