import React, {
  type DetailedHTMLProps,
  type HTMLAttributes,
  type PropsWithChildren,
} from "react";
import styles from "./Navbar.module.css";
import { Link } from "react-router-dom";
import { HOME_PAGE_PATH, LOGIN_PAGE_PATH } from "../../constants";
import NavCategory, { type SubCategories } from "./NavCategory";

type Props = PropsWithChildren<
  DetailedHTMLProps<HTMLAttributes<HTMLDivElement>, HTMLDivElement>
>;

const categories = {
  Download: undefined,
  Turbo: undefined,
  Discover: undefined,
  Safety: {
    Resources: [
      "Family Center",
      "Safety Library",
      "Safety News",
      "Teen Charter",
    ],
    Hubs: [
      "Parent Hub",
      "Policy Hub",
      "Privacy Hub",
      "Transparency Hub",
      "Wellbeing Hub",
    ],
  },
  Quests: {
    Resources: ["Advertising", "Success Stories", "Quests FAQ"],
  },
  Support: {
    Resources: ["Help Center", "Feedback", "Submit a Request"],
  },
  Blog: {
    Collections: [
      "Featured",
      "Community",
      "Discord HQ",
      "Engineering & Developers",
      "How to Discord",
      "Policy & Safety",
      "Product & Features",
    ],
  },
  Developers: {
    flexDirection: "column",
    Featured: ["Discord Social SDK", "Apps and Activities"],
    Documentation: [
      "Developer Home",
      "Developer Documentation",
      "Developer Applications",
      "Developer Help Center",
      "Developer Newsletter",
    ],
  },
  Careers: undefined,
};

function Navbar({}: Props) {
  return (
    <nav className={styles.container}>
      <Link
        to={HOME_PAGE_PATH}
        className={`${styles.buttonLink} ${styles.brandLogo}`}
      >
        Strife
      </Link>
      <ul className={styles.navCategoryContainer}>
        {Object.entries(categories).map(([category, subCategories]) => (
          <NavCategory key={category} name={category} subCategories={subCategories as SubCategories} />
        ))}
      </ul>
      <Link
        to={LOGIN_PAGE_PATH}
        className={`${styles.buttonLink} ${styles.loginButton}`}
      >
        Log In
      </Link>
    </nav>
  );
}

export default Navbar;
