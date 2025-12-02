import React from "react";
import styles from "./NavFooter.module.css";
import Icon from "../Icon";

const links = {
  Product: ["Download", "Turbo", "Status", "App Directory"],
  Company: ["About", "Jobs", "Brand", "Newsroom", "Fall Release"],
  Resources: [
    "Support",
    "Safety",
    "Blog",
    "Creators",
    "Community",
    "Developers",
    "Quests",
    "Official 3rd Party Merch",
    "Feedback",
  ],
  Policies: [
    "Terms",
    "Privacy",
    "Cookie Settings",
    "Guidelines",
    "Acknowledgements",
    "Licenses",
    "Company Information",
  ],
};

function Socials() {
  return (
    <div className={styles.socialsContainer}>
      <Icon className={styles.logoImage} name='person-circle' />
      <div>
        <p className={styles.socialSectionHeading}>Language</p>
        <div className={styles.languageSelector}>
          English
          <Icon className={styles.arrow} name='chevron-down' />
        </div>
      </div>
      <div>
        <p className={styles.socialSectionHeading}>Social</p>
        <div className={styles.socialLinkContainer}>
          <Icon className={styles.socialIcon} name='twitter-x' />
          <Icon className={styles.socialIcon} name='instagram' />
          <Icon className={styles.socialIcon} name='facebook' />
          <Icon className={styles.socialIcon} name='youtube' />
          <Icon className={styles.socialIcon} name='tiktok' />
        </div>
      </div>
    </div>
  );
}

function Links() {
  return (
    <div className={styles.linksContainer}>
      {Object.entries(links).map(([section, items]) => (
        <div className={styles.linksSection} key={section}>
          {section}
          {items.map((link) => (
            <label className={styles.linkLabel} key={link}>
              {link}
            </label>
          ))}
        </div>
      ))}
    </div>
  );
}

type Props = {};

function NavFooter({}: Props) {
  return (
    <div className={styles.container}>
      <Socials />
      <Links />
      <label className={styles.brandLabel}>Strife</label>
    </div>
  );
}

export default NavFooter;
